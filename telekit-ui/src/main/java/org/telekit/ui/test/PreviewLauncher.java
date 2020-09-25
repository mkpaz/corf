package org.telekit.ui.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.LauncherDefaults;
import org.telekit.base.UILoader;
import org.telekit.base.fx.Controller;
import org.telekit.ui.Launcher;

import java.io.InputStream;
import java.util.Objects;

public class PreviewLauncher extends Application implements LauncherDefaults {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Controller controller = UILoader.load(Launcher.getResource("/assets/test/preview.fxml"));

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