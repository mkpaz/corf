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
import org.telekit.base.plugin.DependencyModule;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.util.CommonUtils;
import org.telekit.ui.domain.CloseEvent;
import org.telekit.ui.domain.PluginContainer;
import org.telekit.ui.main.MainController;
import org.telekit.ui.main.Views;
import org.telekit.ui.service.ExceptionHandler;
import org.telekit.ui.service.MainDependencyModule;
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
import static org.telekit.ui.main.AllMessageKeys.MAIN_TRAY_OPEN;
import static org.telekit.ui.main.AllMessageKeys.QUIT;

public class Launcher extends Application implements LauncherDefaults {

    public static final int RESTART_EXIT_CODE = 3;

    public static final String APP_ICON_PATH = "/assets/images/telekit.png";
    public static final String APP_PROPS_PATH = "/assets/application.properties";
    public static final String LOG_CONFIG_FILE_NAME = "logging.properties";
    public static final String LOG_OUTPUT_FILE_NAME = "telekit.log";
    public static final String I18N_RESOURCES_PATH = "org.telekit.ui.i18n.messages";

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
        exceptionHandler = new ExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(
                (thread, throwable) -> exceptionHandler.showErrorDialog(throwable)
        );

        Environment.putIcon(ICON_APP, new Image(getResourceAsStream(APP_ICON_PATH)));
        EventBus.getInstance().subscribe(CloseEvent.class, this::close);

        // init & run application
        initialize();

        MainController controller =
                (MainController) UILoader.load(Views.MAIN_WINDOW.getLocation(), Messages.getInstance());
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle(Environment.APP_NAME);
        primaryStage.getIcons().add(Environment.getIcon(ICON_APP));
        primaryStage.setOnCloseRequest(t -> Platform.exit());

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

        primaryStage.setScene(
                new Scene(controller.getParent(), bounds.getWidth(), bounds.getHeight())
        );
        primaryStage.show();

        if (preferences.isSystemTray()) {
            createTrayIcon(primaryStage);
        }

        Platform.runLater(() -> {
            primaryStage.toFront();
            primaryStage.requestFocus();
        });
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

    private void initialize() throws Exception {
        setupLogging();
        logEnvironmentInfo();
        setSystemProperties();
        createUserResources();

        // cleanup previously uninstalled plugins
        PluginCleaner cleaner = new PluginCleaner();
        cleaner.executeAllSilently();

        // load preferences
//        ApplicationPreferences preferences;
//        XmlMapper mapper = MainDependencyModule.createDefaultMapper();
//        if (Files.exists(ApplicationPreferences.CONFIG_PATH)) {
//            preferences = ApplicationPreferences.load(mapper, ApplicationPreferences.CONFIG_PATH);
//        } else {
//            preferences = new ApplicationPreferences();
//            ApplicationPreferences.store(preferences, mapper, ApplicationPreferences.CONFIG_PATH);
//        }

        // load plugins
        PluginManager pluginManager = new PluginManager();

        List<DependencyModule> modules = new ArrayList<>();
        modules.add(new MainDependencyModule(pluginManager));

        for (PluginContainer container : pluginManager.getAllPlugins()) {
            Plugin plugin = container.getPlugin();
            modules.addAll(plugin.getModules());
        }

        // configure application context
        applicationContext.configure(modules);
        this.preferences = applicationContext.getBean(ApplicationPreferences.class);

        pluginManager.loadPlugins(preferences.getDisabledPlugins());
        pluginManager.setStatus(preferences.getDisabledPlugins(), PluginContainer.Status.DISABLED);

        loadResourceBundles();
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
            Screen.getScreens()
                    .forEach(screen -> logger.info(
                            "Screen: bounds=" + screen.getVisualBounds() + "; dpi=" + screen.getDpi()
                    ));

            // iterate through each locale and print
            // locale code, display name and country
            Locale[] locales = SimpleDateFormat.getAvailableLocales();
            logger.info("Supported locales:");
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

    private void loadResourceBundles() {
        Messages.getInstance().load(
                MessagesBundleProvider.getBundle(preferences.getLocale()),
                Messages.class.getName()
        );
        Messages.getInstance().load(
                ResourceBundle.getBundle(I18N_RESOURCES_PATH, preferences.getLocale(), Launcher.class.getModule()),
                Launcher.class.getName()
        );
    }

    // - JavaFX doesn't support system tray at all
    // - AWT system tray support is outdated and it may not work in KDE and Gnome3+
    // - dorkbox.SystemTray library is the best option but...
    //   it's not JDK11 compliant because it relies on JDK internal API (com.sun.*)
    // You may choose whatewer you like :)
    private void createTrayIcon(Stage primaryStage) {
        // example of dorkbox.SystemTray
        //try {
        //    SystemTray systemTray = SystemTray.get();
        //    if (systemTray == null) {
        //        logger.warning("Unable to load system tray");
        //        return;
        //    }
        //
        //    systemTray.setImage(getResourceAsStream(APP_ICON_PATH));
        //    Menu trayMenu = systemTray.getMenu();
        //
        //    MenuItem showItem = new MenuItem(Messages.get(MAIN_TRAY_OPEN), e -> {
        //        if (primaryStage.isShowing()) {
        //            Platform.runLater(primaryStage::toFront);
        //        } else {
        //            Platform.runLater(primaryStage::show);
        //        }
        //    });
        //    trayMenu.add(showItem);
        //
        //    MenuItem quitItem = new MenuItem(Messages.get(QUIT), e -> {
        //        Platform.runLater(() -> EventBus.getInstance().publish(new CloseEvent(exitCode)));
        //    });
        //    trayMenu.add(quitItem);
        //
        //} catch (Throwable t) {
        //    logger.warning(ExceptionUtils.getStackTrace(t));
        //}

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

            java.awt.Image trayImage = SwingFXUtils.fromFXImage(Environment.getIcon(ICON_APP), null);
            TrayIcon trayIcon = new TrayIcon(trayImage, APP_NAME, trayMenu);

            SystemTray tray = SystemTray.getSystemTray();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                logger.warning(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void setSystemProperties() {
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
    }

    static boolean isScreenFits(int width, int height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() > width && screenBounds.getHeight() > height;
    }
}