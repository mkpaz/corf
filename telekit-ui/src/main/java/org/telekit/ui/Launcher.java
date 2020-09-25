package org.telekit.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.*;
import org.telekit.base.EventBus.Listener;
import org.telekit.base.i18n.Messages;
import org.telekit.base.i18n.MessagesBundleProvider;
import org.telekit.base.plugin.DependencyModule;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.util.CommonUtils;
import org.telekit.ui.domain.CloseEvent;
import org.telekit.ui.domain.PluginContainer;
import org.telekit.ui.main.MainController;
import org.telekit.ui.main.Views;
import org.telekit.ui.service.ExceptionHandler;
import org.telekit.ui.service.PluginCleaner;
import org.telekit.ui.service.PluginManager;

import javax.net.ssl.SSLServerSocketFactory;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.telekit.base.Environment.*;
import static org.telekit.base.IconCache.ICON_APP;
import static org.telekit.ui.main.MessageKeys.MAIN_TRAY_OPEN;
import static org.telekit.ui.main.MessageKeys.QUIT;

public class Launcher extends Application implements LauncherDefaults {

    public static final int RESTART_EXIT_CODE = 3;

    public static final String APP_ICON_PATH = "/assets/images/telekit.png";
    public static final String APP_PROPS_PATH = "/assets/application.properties";
    public static final String LOG_CONFIG_FILE_NAME = "logging.properties";
    public static final String LOG_OUTPUT_FILE_NAME = "telekit.log";
    public static final String I18N_RESOURCES_PATH = "org.telekit.ui.i18n.messages";
    public static final String THEMES_DIR_PATH = "/assets/themes/";
    public static final String INDEX_CSS_PATH = "/assets/ui/index.css";

    private static int exitCode = 0;
    private ApplicationContext applicationContext = ApplicationContext.getInstance();
    private Logger logger;
    private ExceptionHandler exceptionHandler;
    private ApplicationPreferences preferences;

    public static void main(String[] args) {
        launch(args);
        System.exit(exitCode);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // set exception handler first
        exceptionHandler = new ExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(
                (thread, throwable) -> exceptionHandler.showErrorDialog(throwable)
        );

        // initialize application and set class-level variables
        initialize();

        // create main controller
        MainController controller = (MainController) UILoader.load(Views.MAIN_WINDOW.getLocation(), Messages.getInstance());
        controller.setPrimaryStage(primaryStage);

        // populate icon cache
        IconCache.put(ICON_APP, new Image(getResourceAsStream(APP_ICON_PATH)));
        primaryStage.getIcons().add(IconCache.get(ICON_APP));

        // handle application close events
        EventBus.getInstance().subscribe(CloseEvent.class, this::close);
        primaryStage.setOnCloseRequest(t -> Platform.exit());

        // set main window size
        Dimension bounds = isScreenFits(PREF_WIDTH, PREF_HEIGHT) ?
                new Dimension(PREF_WIDTH, PREF_HEIGHT) :
                new Dimension(MIN_WIDTH, MIN_HEIGHT);
        if (FORCE_WINDOW_SIZE != null) {
            bounds = FORCE_WINDOW_SIZE;
        } else {
            primaryStage.setMaximized(true);
        }
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        // create scene and apply (TBD: user selected) theme to it
        Scene scene = new Scene(controller.getParent(), bounds.getWidth(), bounds.getHeight());
        scene.getStylesheets().add(getResource(INDEX_CSS_PATH).toExternalForm());
        scene.getStylesheets().add(getResource(THEMES_DIR_PATH + "base.css").toExternalForm());

        // show primary stage
        primaryStage.setTitle(Environment.APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.runLater(() -> {
            primaryStage.toFront();
            primaryStage.requestFocus();
        });

        // create tray icon (won't work in some Linux DE)
        if (preferences.isSystemTray()) {
            createTrayIcon(primaryStage);
        }
    }

    @NotNull
    public static URL getResource(String resource) {
        return Objects.requireNonNull(Launcher.class.getResource(resource));
    }

    @NotNull
    public static InputStream getResourceAsStream(String resource) {
        return Objects.requireNonNull(Launcher.class.getResourceAsStream(resource));
    }

    @Listener
    public void close(CloseEvent event) {
        exitCode = event.getExitCode();
        Platform.exit();
    }

    ///////////////////////////////////////////////////////////////////////////

    // ATTENTION: code order matters, some of these methods initialize class level variables
    private void initialize() throws Exception {
        setupLogging(); // initializes logger variable
        logEnvironmentInfo();
        loadApplicationProperties(); // load properties from application.properties file
        createUserResources();

        // cleanup previously uninstalled plugins
        PluginCleaner cleaner = new PluginCleaner();
        cleaner.executeAllSilently();

        // collect DI modules
        List<DependencyModule> modules = new ArrayList<>();
        PluginManager pluginManager = new PluginManager();
        modules.add(new MainDependencyModule(pluginManager));
        for (PluginContainer container : pluginManager.getAllPlugins()) {
            Plugin plugin = container.getPlugin();
            modules.addAll(plugin.getModules());
        }

        // configure application context
        applicationContext.configure(modules);
        preferences = applicationContext.getBean(ApplicationPreferences.class);

        // load plugins
        pluginManager.loadPlugins(preferences.getDisabledPlugins());
        pluginManager.setStatus(preferences.getDisabledPlugins(), PluginContainer.Status.DISABLED);

        // load resource bundles
        Messages.getInstance().load(
                MessagesBundleProvider.getBundle(preferences.getLocale()),
                Messages.class.getName()
        );
        Messages.getInstance().load(
                ResourceBundle.getBundle(I18N_RESOURCES_PATH, preferences.getLocale(), Launcher.class.getModule()),
                Launcher.class.getName()
        );
    }

    private void setupLogging() {
        try {
            LogManager logManager = LogManager.getLogManager();
            Path configPath = DATA_DIR.resolve(LOG_CONFIG_FILE_NAME);
            if (Files.exists(configPath)) {
                String outputPath = APP_DIR.resolve(LOG_OUTPUT_FILE_NAME).toString();

                // JUL can't handle Windows-style paths properly
                if (CommonUtils.isWindows()) {
                    outputPath = outputPath.replace("\\", "/");
                }

                List<String> configData = Files.readAllLines(configPath, StandardCharsets.UTF_8);
                configData.add("java.util.logging.FileHandler.pattern=" + outputPath);
                logManager.readConfiguration(
                        new ByteArrayInputStream(String.join("\n", configData).getBytes())
                );
            }

            logger = Logger.getLogger(this.getClass().getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logEnvironmentInfo() {
        try {
            logger.info("OS=" + System.getProperty("os.name"));
            logger.info("OS arch=" + System.getProperty("os.arch"));
            Screen.getScreens().forEach(screen -> logger.info(
                    "Screen: bounds=" + screen.getVisualBounds() + "; dpi=" + screen.getDpi()
            ));

            logger.info("Supported locales:");
            Locale[] locales = SimpleDateFormat.getAvailableLocales();
            for (Locale locale : locales) {
                logger.info(locale.toString() + " / " + locale.getDisplayName());
            }

            logger.info("Supported SSL/TLS ciphers:");
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            String[] ciphers = sslSocketFactory.getDefaultCipherSuites();
            for (String cipherSuite : ciphers) {
                logger.info(cipherSuite);
            }
        } catch (Throwable ignored) { }
    }

    // - JavaFX doesn't support system tray at all
    // - AWT system tray support is outdated and it may not work in KDE and Gnome3+
    // - dorkbox.SystemTray library is the best option but it's not JDK11 compliant
    //   because it relies on JDK internal API (com.sun.*)
    // You may choose whatever you like :)
    private void createTrayIcon(Stage primaryStage) {
        String xdgCurrentDesktop = System.getenv("XDG_CURRENT_DESKTOP");
        boolean badTraySupport = xdgCurrentDesktop != null && (
                xdgCurrentDesktop.toLowerCase().contains("kde") |
                        xdgCurrentDesktop.toLowerCase().contains("gnome")
        );

        if (SystemTray.isSupported() && badTraySupport) {
            Platform.setImplicitExit(false);
            primaryStage.setOnCloseRequest(t -> Platform.runLater(primaryStage::hide));

            PopupMenu trayMenu = new PopupMenu();

            MenuItem showItem = new MenuItem(Messages.get(MAIN_TRAY_OPEN));
            ActionListener showListener = e -> {
                if (primaryStage.isShowing()) {
                    Platform.runLater(primaryStage::toFront);
                } else {
                    Platform.runLater(primaryStage::show);
                }
            };
            showItem.addActionListener(showListener);
            trayMenu.add(showItem);

            MenuItem closeItem = new MenuItem(Messages.get(QUIT));
            ActionListener closeListener = e ->
                    Platform.runLater(() -> EventBus.getInstance().publish(new CloseEvent(exitCode)));
            closeItem.addActionListener(closeListener);
            trayMenu.add(closeItem);

            java.awt.Image trayImage = SwingFXUtils.fromFXImage(IconCache.get(ICON_APP), null);
            TrayIcon trayIcon = new TrayIcon(trayImage, APP_NAME, trayMenu);

            SystemTray tray = SystemTray.getSystemTray();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                logger.warning(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void loadApplicationProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(getResourceAsStream(APP_PROPS_PATH), StandardCharsets.UTF_8));
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                System.setProperty(
                        String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void createUserResources() throws Exception {
        if (!Files.exists(DATA_DIR)) Files.createDirectory(DATA_DIR);
        if (!Files.exists(PLUGINS_DIR)) Files.createDirectory(PLUGINS_DIR);
        if (!Files.exists(APP_TEMP_DIR)) Files.createDirectory(APP_TEMP_DIR);
    }

    private static boolean isScreenFits(int width, int height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() > width && screenBounds.getHeight() > height;
    }
}