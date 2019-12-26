package corf.desktop;

import corf.base.Injector;
import corf.base.desktop.Dimension;
import corf.base.event.ActionEvent;
import corf.base.event.BrowseEvent;
import corf.base.event.Events;
import corf.base.plugin.DependencyModule;
import corf.base.plugin.internal.PluginException;
import corf.base.plugin.internal.PluginManager;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.desktop.layout.MainStage;
import corf.desktop.layout.MainWindowView;
import corf.desktop.startup.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Launcher extends Application {

    static {
        LogConfig.setupLogging();
    }

    @SuppressWarnings("NullAway")
    private ApplicationPreferences preferences;

    @SuppressWarnings("NullAway")
    private PluginManager pluginManager;

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) {
        // create exception handler and set it to the main/FX thread
        Thread.currentThread().setUncaughtExceptionHandler(Config.DEFAULT_EXCEPTION_HANDLER);

        // subscribe on events
        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.APP_CLOSE_REQUEST)) { close(primaryStage); }
        });
        Events.listen(BrowseEvent.class, this::onBrowseEvent);

        // init application
        final var mainStage = initApplication(primaryStage);
        primaryStage.setOnCloseRequest(e -> storeStageSize(primaryStage));

        // load main view
        var mainView = Injector.getView(MainWindowView.class);
        mainStage.setContent(mainView);

        // show application
        Platform.runLater(() -> {
            mainStage.show();
            // on Windows calling toFront() or requestFocus() doesn't move
            // on top automatically, but instead makes the task bar app button blink
            // https://bugs.openjdk.java.net/browse/JDK-8128222
            primaryStage.toFront();
        });
    }

    @Override
    public void stop() {
        try {
            pluginManager.stopAllPlugins();
        } catch (PluginException ignored) {
            // even if some plugins weren't stopped,
            // it shouldn't prevent application from shutting down
        }
    }

    private void close(Stage primaryStage) {
        storeStageSize(primaryStage);
        Platform.exit();
    }

    private void storeStageSize(Stage primaryStage) {
        if (preferences != null) {
            var stageSize = Dimension.of(primaryStage);
            preferences.getSystemPreferences().setMainWindowSize(stageSize);
        }
    }

    private MainStage initApplication(Stage primaryStage) {
        var logConfig = new LogConfig();
        logConfig.logEnvironmentInfo();

        var preferencesConfig = new PreferencesConfig();
        preferences = preferencesConfig.getPreferences();

        var pluginConfig = new PluginConfig(preferencesConfig.getPreferences());
        pluginManager = pluginConfig.getPluginManager();
        pluginConfig.startPlugins();

        var servicesConfig = new ServicesConfig();
        servicesConfig.startServices();

        var mainStage = MainStage.create(primaryStage, preferences);

        // collect all modules and initialize the injector
        var modules = new ArrayList<DependencyModule>();
        var mainModule = new MainDependencyModule(
                mainStage,
                preferencesConfig,
                pluginConfig,
                servicesConfig,
                logConfig
        );
        modules.add(mainModule);
        modules.addAll(pluginConfig.getDependencyModules());
        Injector.getInstance().configure(modules);

        return mainStage;
    }

    private void onBrowseEvent(BrowseEvent event) {
        getHostServices().showDocument(event.getUri().toString());
    }
}
