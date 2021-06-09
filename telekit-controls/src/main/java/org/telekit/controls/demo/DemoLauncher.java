package org.telekit.controls.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.I18n;
import org.telekit.controls.i18n.ControlsMessages;
import org.telekit.controls.theme.DefaultTheme;

import java.util.Locale;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class DemoLauncher extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Locale.setDefault(defaultIfNull(Env.LOCALE, Locale.getDefault()));
        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().register(ControlsMessages.getLoader());
        I18n.getInstance().reload();

        DemoController controller = ViewLoader.load(DemoController.class);

        Scene scene = new Scene(controller.getRoot(), 1024, 768);
        scene.getStylesheets().addAll(new DefaultTheme().getResources());

        primaryStage.setTitle("Components Overview");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }
}