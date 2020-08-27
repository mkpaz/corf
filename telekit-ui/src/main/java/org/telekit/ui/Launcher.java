package org.telekit.ui;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.*;
import org.telekit.base.EventBus.Listener;
import org.telekit.base.internal.UserPreferences;
import org.telekit.base.plugin.DependencyModule;
import org.telekit.base.plugin.Plugin;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.telekit.base.Settings.*;

public class Launcher extends Application implements LauncherDefaults {

    public static final int RESTART_EXIT_CODE = 3;

    public static final String APP_ICON_PATH = "/assets/images/telekit.png";
    public static final String APP_PROPS_PATH = "/assets/application.properties";
    public static final String LOG_CONFIG_FILE_NAME = "logging.properties";
    public static final String LOG_OUTPUT_FILE_NAME = "telekit.log";

    private static int exitCode = 0;
    private ApplicationContext applicationContext = ApplicationContext.getInstance();
    private ExceptionHandler exceptionHandler;

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

        Settings.putIcon(ICON_APP, new Image(getResourceAsStream(APP_ICON_PATH)));
        EventBus.getInstance().subscribe(CloseEvent.class, this::close);

        // init & run application
        initialize();

        MainController controller = (MainController) UILoader.load(Views.MAIN_WINDOW.getLocation());
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle(Settings.APP_NAME);
        primaryStage.getIcons().add(Settings.getIcon(ICON_APP));
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
        createResources();

        // cleanup previously uninstalled plugins
        PluginCleaner cleaner = new PluginCleaner();
        cleaner.executeAllSilently();

        // load preferences
        UserPreferences preferences;
        XmlMapper mapper = MainDependencyModule.createDefaultMapper();
        if (Files.exists(UserPreferences.CONFIG_PATH)) {
            preferences = UserPreferences.load(mapper, UserPreferences.CONFIG_PATH);
        } else {
            preferences = new UserPreferences();
            UserPreferences.store(preferences, mapper, UserPreferences.CONFIG_PATH);
        }

        // load plugins
        PluginManager pluginManager = new PluginManager();
        pluginManager.loadPlugins(preferences.getDisabledPlugins());

        List<DependencyModule> modules = new ArrayList<>();
        modules.add(new MainDependencyModule(pluginManager));

        for (PluginContainer container : pluginManager.getAllPlugins()) {
            Plugin plugin = container.getPlugin();
            modules.addAll(plugin.getModules());
        }

        pluginManager.setStatus(preferences.getDisabledPlugins(), PluginContainer.Status.DISABLED);

        // configure application context
        applicationContext.configure(modules);
        applicationContext.getBean(Settings.class).setPreferences(preferences);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logEnvironmentInfo() {
        Logger logger = Logger.getLogger(this.getClass().getName());

        try {
            logger.info("OS=" + System.getProperty("os.name"));
            logger.info("OS arch=" + System.getProperty("os.arch"));
            Screen.getScreens().forEach(screen ->
                    logger.info("Screen: bounds=" + screen.getVisualBounds() + "; dpi=" + screen.getDpi())
            );

            logger.info("Supported SSL/TLS ciphers:");
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            String[] ciphers = sslSocketFactory.getDefaultCipherSuites();
            for (String cipherSuite : ciphers) {
                logger.info(cipherSuite);
            }
        } catch (Throwable ignored) {
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

    private void createResources() throws Exception {
        if (!Files.exists(DATA_DIR)) Files.createDirectory(DATA_DIR);
        if (!Files.exists(PLUGINS_DIR)) Files.createDirectory(PLUGINS_DIR);
    }

    static boolean isScreenFits(int width, int height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() > width && screenBounds.getHeight() > height;
    }
}