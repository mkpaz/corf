package org.telekit.controls.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.i18n.BaseMessagesBundleProvider;
import org.telekit.base.i18n.Messages;
import org.telekit.controls.i18n.ControlsMessagesBundleProvider;
import org.telekit.controls.theme.ThemeLoader;

import java.util.Locale;

public class DemoLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Messages.getInstance().load(
                ControlsMessagesBundleProvider.getBundle(Locale.getDefault()),
                ControlsMessagesBundleProvider.class.getName()
        );
        Messages.getInstance().load(
                BaseMessagesBundleProvider.getBundle(Locale.getDefault()),
                BaseMessagesBundleProvider.class.getName()
        );

        DemoController controller = ViewLoader.load(DemoController.class);

        Scene scene = new Scene(controller.getRoot(), 1024, 768);
        ThemeLoader themeLoader = new ThemeLoader();
        scene.getStylesheets().addAll(themeLoader.getStylesheets(""));

        primaryStage.setTitle("Components Overview");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }
}