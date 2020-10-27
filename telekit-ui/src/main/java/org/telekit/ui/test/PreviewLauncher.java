package org.telekit.ui.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.ui.LauncherDefaults;
import org.telekit.base.ui.UILoader;
import org.telekit.base.ui.Controller;
import org.telekit.base.i18n.BaseMessagesBundleProvider;
import org.telekit.base.i18n.Messages;
import org.telekit.controls.i18n.ControlsMessagesBundleProvider;
import org.telekit.ui.Launcher;

import java.io.InputStream;
import java.util.Objects;
import java.util.ResourceBundle;

public class PreviewLauncher extends Application implements LauncherDefaults {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Messages.getInstance().load(
                ControlsMessagesBundleProvider.getBundle(Env.LOCALE),
                ControlsMessagesBundleProvider.class.getName()
        );
        Messages.getInstance().load(
                BaseMessagesBundleProvider.getBundle(Env.LOCALE),
                BaseMessagesBundleProvider.class.getName()
        );
        Messages.getInstance().load(
                ResourceBundle.getBundle(Launcher.I18N_RESOURCES_PATH, Objects.requireNonNull(Env.LOCALE), Launcher.class.getModule()),
                Launcher.class.getName()
        );

        Controller controller = UILoader.load(
                Launcher.getResource("/assets/test/preview.fxml"),
                Messages.getInstance()
        );

        // create scene and apply (TBD: user selected) theme to it
        Scene scene = new Scene(controller.getParent(), 1024, 768);
        scene.getStylesheets().add(Launcher.getResource(Launcher.INDEX_CSS_PATH).toExternalForm());
        scene.getStylesheets().add(Launcher.getResource(Launcher.THEMES_DIR_PATH + "base.css").toExternalForm());

        primaryStage.setTitle("Demo");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    private static InputStream getResourceAsStream(String resource) {
        return Objects.requireNonNull(PreviewLauncher.class.getResourceAsStream(resource));
    }
}