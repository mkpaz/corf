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
import org.telekit.controls.i18n.ControlsMessages;
import org.telekit.controls.theme.DefaultTheme;
import org.telekit.controls.widgets.OverlayBase;

import javax.inject.Singleton;
import java.util.*;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public abstract class BaseLauncher extends Application {

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
        modules.add(new DemoDependencyModule());
        modules.addAll(getDependencyModules());
        Injector.getInstance().configure(modules);

        Overlay overlay = Injector.getInstance().getBean(Overlay.class);

        component = ViewLoader.load(getComponent());
        component.getRoot().setStyle("-fx-background-color: -fx-background;");

        root = new StackPane();
        root.getChildren().setAll((OverlayBase) overlay, component.getRoot());
        overlay.toBack();

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().addAll(new DefaultTheme().getResources());

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

    private static class DemoDependencyModule implements DependencyModule {

        @Provides
        @Singleton
        public Overlay overlay() {
            return new OverlayBase();
        }
    }
}