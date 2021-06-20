package org.telekit.controls;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.base.i18n.I18n;
import org.telekit.controls.i18n.ControlsMessages;
import org.telekit.controls.theme.DefaultTheme;

import java.util.Collection;
import java.util.Locale;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public abstract class BaseLauncher extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Locale.setDefault(defaultIfNull(Env.LOCALE, Locale.getDefault()));

        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().register(ControlsMessages.getLoader());
        getBundleLoaders().forEach(loader -> I18n.getInstance().register(loader));
        I18n.getInstance().reload();

        Component root = ViewLoader.load(getComponent());

        Scene scene = new Scene(root.getRoot(), 1024, 768);
        scene.getStylesheets().addAll(new DefaultTheme().getResources());

        initLauncher(primaryStage, scene);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    protected abstract Class<? extends Component> getComponent();

    protected abstract Collection<BundleLoader> getBundleLoaders();

    /** Reserved for startup customizations. Override when necessary. */
    protected void initLauncher(Stage stage, Scene scene) {}
}