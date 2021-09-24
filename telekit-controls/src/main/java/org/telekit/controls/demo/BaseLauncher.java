package org.telekit.controls.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.telekit.base.Env;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.di.DependencyModule;
import org.telekit.base.di.Injector;
import org.telekit.base.di.Provides;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.base.i18n.I18n;
import org.telekit.base.preferences.SharedPreferences;
import org.telekit.controls.i18n.ControlsMessages;
import org.telekit.controls.theme.DefaultTheme;
import org.telekit.controls.util.NodeUtils;
import org.telekit.controls.widgets.OverlayBase;

import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Logger;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public abstract class BaseLauncher extends Application {

    private static final Logger LOG = Logger.getLogger(BaseLauncher.class.getName());

    protected StackPane root;
    protected Component component;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());
        DefaultEventBus.getInstance().subscribe(Notification.class, n -> {
            System.out.println("Notification [" + n.getType() + "]");
            System.out.println("------------------------------------");
            System.out.println("message:" + n.getText());
            if (n.getThrowable() != null) {
                System.out.println(ExceptionUtils.getStackTrace(n.getThrowable()));
            }
        });

        Locale.setDefault(defaultIfNull(Env.LOCALE, Locale.getDefault()));

        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().register(ControlsMessages.getLoader());
        getBundleLoaders().forEach(loader -> I18n.getInstance().register(loader));
        I18n.getInstance().reload();

        initServices();

        List<DependencyModule> modules = new ArrayList<>();
        modules.add(new BaseLauncherDependencyModule());
        modules.addAll(getDependencyModules());
        Injector.getInstance().configure(modules);

        Overlay overlay = Injector.getInstance().getBean(Overlay.class);

        component = ViewLoader.load(getComponent());
        component.getRoot().setStyle("-fx-background-color: -fx-background;");

        root = new StackPane();
        root.getChildren().setAll((OverlayBase) overlay, component.getRoot());
        ((OverlayBase) overlay).onFrontProperty().addListener((obs, old, value) -> {
            if (!value) {
                NodeUtils.begForFocus(component.getRoot(), 3);
            } else {
                NodeUtils.begForFocus(overlay.getContent(), 3);
            }
        });

        overlay.toBack();

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().addAll(new DefaultTheme().getResources());
        if (Env.isDevMode()) {
            scene.focusOwnerProperty().addListener((obs, old, value) -> {
                LOG.info("focus owner was: " + old);
                LOG.info("focus owner is: " + value);
            });
        }

        initStage(primaryStage, scene);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    protected abstract Class<? extends Component> getComponent();

    protected abstract Collection<BundleLoader> getBundleLoaders();

    protected List<DependencyModule> getDependencyModules() {
        return Collections.emptyList();
    }

    /** Reserved for startup customizations. Override when necessary. */
    protected void initServices() {}

    /** Reserved for startup customizations. Override when necessary. */
    protected void initStage(Stage stage, Scene scene) {}

    ///////////////////////////////////////////////////////////////////////////

    private static class BaseLauncherDependencyModule implements DependencyModule {

        @Provides
        @Singleton
        public Overlay overlay() {
            return new OverlayBase();
        }

        @Provides
        @Singleton
        public SharedPreferences sharedPreferences() {
            return new DemoSharedPreferences();
        }
    }
}