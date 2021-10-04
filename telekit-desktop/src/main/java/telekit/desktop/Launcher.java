package telekit.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import telekit.base.desktop.ViewLoader;
import telekit.base.di.DependencyModule;
import telekit.base.di.Injector;
import telekit.base.event.DefaultEventBus;
import telekit.base.event.Listener;
import telekit.base.plugin.internal.PluginException;
import telekit.base.plugin.internal.PluginManager;
import telekit.base.preferences.internal.ApplicationPreferences;
import telekit.desktop.event.CloseRequestEvent;
import telekit.desktop.startup.MainDependencyModule;
import telekit.desktop.startup.config.*;
import telekit.desktop.views.MainStage;
import telekit.desktop.views.layout.MainWindowView;

import java.util.ArrayList;
import java.util.List;

public class Launcher extends Application {

    public static final int DEFAULT_EXIT_CODE = 0;
    public static final int RESTART_EXIT_CODE = 3;

    private static int exitCode = DEFAULT_EXIT_CODE;

    private ApplicationPreferences preferences;
    private PluginManager pluginManager;

    public static void main(String[] args) {
        launch(args);
        System.exit(exitCode);
    }

    @Override
    public void start(Stage primaryStage) {
        // create exception handler and set it to the main/FX thread
        Thread.currentThread().setUncaughtExceptionHandler(Config.DEFAULT_EXCEPTION_HANDLER);

        // subscribe on close events
        DefaultEventBus.getInstance().subscribe(CloseRequestEvent.class, this::close);

        // configure application and set class-level variables
        final MainStage mainStage = configure(primaryStage);

        // init view hierarchy
        MainWindowView mainView = ViewLoader.load(MainWindowView.class);
        mainStage.setContent(mainView);

        // show application
        Platform.runLater(() -> {
            mainStage.show();
            mainStage.toFront();
            primaryStage.requestFocus();
        });
    }

    @Override
    public void stop() {
        try {
            pluginManager.stopAllPlugins();
        } catch (PluginException ignored) {
            // even if some plugin wasn't stopped, it shouldn't prevent application from shutting down
        }
    }

    @Listener
    public void close(CloseRequestEvent event) {
        exitCode = event.getExitCode();
        if (preferences != null && event.getWindowSize() != null) {
            preferences.getSystemPreferences().setMainWindowSize(event.getWindowSize());
        }
        Platform.exit();
    }

    private MainStage configure(Stage primaryStage) {
        LogConfig logConfig = new LogConfig();
        logConfig.logEnvironmentInfo();

        PreferencesConfig preferencesConfig = new PreferencesConfig();
        preferences = preferencesConfig.getPreferences();

        SecurityConfig securityConfig = new SecurityConfig(preferences);
        preferencesConfig.savePreferences(); // save preferences if changes were made

        PluginConfig pluginConfig = new PluginConfig(preferencesConfig.getPreferences());
        pluginManager = pluginConfig.getPluginManager();
        pluginConfig.startPlugins();

        ServicesConfig servicesConfig = new ServicesConfig();
        servicesConfig.startServices();

        MainStage mainStage = MainStage.createUndecorated(primaryStage, preferences);
        mainStage.getScene().getStylesheets().addAll(pluginConfig.getStylesheets());

        // collect all modules and initialize injector
        List<DependencyModule> modules = new ArrayList<>();
        MainDependencyModule mainModule = new MainDependencyModule(
                mainStage,
                preferencesConfig,
                securityConfig,
                pluginConfig,
                servicesConfig,
                logConfig
        );
        modules.add(mainModule);
        modules.addAll(pluginConfig.getDependencyModules());
        Injector.getInstance().configure(modules);

        return mainStage;
    }
}