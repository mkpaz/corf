package org.telekit.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.telekit.base.ApplicationContext;
import org.telekit.base.LauncherDefaults;
import org.telekit.base.Settings;
import org.telekit.base.plugin.Tool;
import org.telekit.example.service.ExampleDependencyModule;
import org.telekit.example.tools.ExampleController;
import org.telekit.example.tools.ExampleTool;

import java.io.InputStream;
import java.util.Objects;

import static org.telekit.base.Settings.ICON_APP;
import static org.telekit.example.ExamplePlugin.ASSETS_PATH;

public class Launcher extends Application implements LauncherDefaults {

    private final ApplicationContext applicationContext = ApplicationContext.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        applicationContext.configure(new ExampleDependencyModule());

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());
        Settings.putIcon(ICON_APP, new Image(getResourceAsStream(ASSETS_PATH + "images/telekit.png")));

        Tool exampleTool = new ExampleTool();
        ExampleController controller = (ExampleController) exampleTool.createController();

        primaryStage.setTitle(Settings.APP_NAME);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(controller.getParent(), PREF_WIDTH, PREF_HEIGHT));
        primaryStage.getIcons().add(Settings.getIcon(ICON_APP));
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    public static InputStream getResourceAsStream(String resource) {
        return Objects.requireNonNull(Launcher.class.getResourceAsStream(resource));
    }
}
