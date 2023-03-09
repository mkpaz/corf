package corf.base.plugin;

import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import backbonefx.di.Provides;
import backbonefx.mvvm.View;
import corf.base.Env;
import corf.base.Injector;
import corf.base.desktop.Focusable;
import corf.base.desktop.Overlay;
import corf.base.desktop.controls.DrawerPane;
import corf.base.desktop.controls.FXHelpers;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.i18n.I18n;
import corf.base.i18n.M;
import corf.base.preferences.Proxy;
import corf.base.preferences.SharedPreferences;
import corf.base.preferences.SystemPreferences;
import jakarta.inject.Singleton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * A helper class that provides a base for starting a plugin tool
 * as separate GUI app, so the plugin can be developed and tested
 * without installation.
 */
@SuppressWarnings("unused")
public abstract class PluginLauncher<V extends Node> extends Application {

    protected static final System.Logger LOGGER = System.getLogger(PluginLauncher.class.getName());

    @SuppressWarnings("NullAway")
    protected SharedPreferences preferences;

    @SuppressWarnings("NullAway")
    protected StackPane rootContainer;

    @SuppressWarnings("NullAway")
    protected View<V, ?> view;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());
        Events.listen(Notification.class, n -> {
            System.out.println("Notification [" + n.getType() + "] :" + n.getText());
            if (n.getThrowable() != null) {
                System.out.println(ExceptionUtils.getStackTrace(n.getThrowable()));
            }
        });

        Locale.setDefault(ObjectUtils.defaultIfNull(Env.LOCALE, Locale.getDefault()));

        I18n.getInstance().register(M.getLoader());
        I18n.getInstance().reload();

        initServices();

        preferences = getPreferences();

        var modules = new ArrayList<DependencyModule>();
        modules.add(new LauncherDependencyModule(preferences));
        modules.addAll(getDependencyModules());
        Injector.getInstance().configure(modules);

        var scene = createScene();
        initStage(primaryStage, scene);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    protected abstract Class<? extends View<V, ?>> getView();

    protected Scene createScene() {
        var overlay = Injector.getInstance().getBean(Overlay.class);

        view = Injector.getView(getView());
        view.getRoot().setStyle("-fx-background-color: -color-bg-default;");

        rootContainer = new StackPane();
        rootContainer.getChildren().setAll((DrawerPane) overlay, view.getRoot());

        ((DrawerPane) overlay).onFrontProperty().addListener((obs, old, val) -> {
            if (!val) {
                if (view instanceof Focusable focusable && focusable.getPrimaryFocusNode() != null) {
                    focusable.begForFocus(3);
                }
            } else {
                if (overlay.getContent() != null) {
                    FXHelpers.begForFocus(overlay.getContent(), 3);
                }
            }
        });

        overlay.toBack();

        // set style theme
        Application.setUserAgentStylesheet(preferences.getStyleTheme().getUserAgentStylesheet());

        var scene = new Scene(rootContainer, 1024, 768);
        Env.BASE_MODULE.concat("assets/fonts.css").getResource()
                .ifPresent(r -> scene.getStylesheets().add(r.toExternalForm()));
        Env.BASE_MODULE.concat("assets/index.css").getResource()
                .ifPresent(r -> scene.getStylesheets().add(r.toExternalForm()));

        return scene;
    }

    protected String getResource(String path) {
        return Objects.requireNonNull(PluginLauncher.class.getResource(path)).toExternalForm();
    }

    /** Reserved for startup customizations. Override when necessary. */
    protected SharedPreferences getPreferences() {
        return new LauncherSharedPreferences();
    }

    /** Reserved for startup customizations. Override when necessary. */
    protected List<DependencyModule> getDependencyModules() {
        return Collections.emptyList();
    }

    /** Reserved for startup customizations. Override when necessary. */
    protected void initServices() { }

    /** Reserved for startup customizations. Override when necessary. */
    protected void initStage(Stage stage, Scene scene) { }

    ///////////////////////////////////////////////////////////////////////////

    public static class LauncherDependencyModule implements DependencyModule {

        private final SharedPreferences preferences;

        public LauncherDependencyModule(SharedPreferences preferences) {
            this.preferences = Objects.requireNonNull(preferences);
        }

        @Provides
        @Singleton
        public Overlay overlay() {
            return new DrawerPane();
        }

        @Provides
        @Singleton
        public SharedPreferences sharedPreferences() {
            return preferences;
        }
    }

    public static class LauncherSharedPreferences implements SharedPreferences {

        protected static final Preferences USER_ROOT = Preferences.userRoot().node(Env.APP_NAME + "-Demo");

        protected final Theme theme = new PrimerLight();
        protected final SystemPreferences systemPreferences = new SystemPreferences(USER_ROOT);

        @Override
        public @Nullable Proxy getProxy() {
            return null;
        }

        @Override
        public SystemPreferences getSystemPreferences() {
            return systemPreferences;
        }

        @Override
        public Theme getStyleTheme() {
            return theme;
        }
    }
}
