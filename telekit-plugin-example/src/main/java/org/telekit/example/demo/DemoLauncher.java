package org.telekit.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.di.Injector;
import org.telekit.base.i18n.BaseMessagesBundleProvider;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Tool;
import org.telekit.example.ExamplePlugin;
import org.telekit.example.service.ExampleDependencyModule;
import org.telekit.example.tools.ExampleController;
import org.telekit.example.tools.HelloTool;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import static org.telekit.example.ExamplePlugin.ASSETS_PATH;

public class DemoLauncher extends Application {

    private final Injector injector = Injector.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        injector.configure(new ExampleDependencyModule());

        ExamplePlugin plugin = new ExamplePlugin();

        Locale.setDefault(Env.LOCALE);
        Messages.getInstance().load(BaseMessagesBundleProvider.getBundle(Locale.getDefault()), Messages.class.getName());
        Messages.getInstance().load(plugin.getBundle(Locale.getDefault()), DemoLauncher.class.getName());

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Tool exampleTool = new HelloTool();
        ExampleController controller = (ExampleController) exampleTool.createComponent();
        Scene scene = new Scene(controller.getRoot(), 1440, 900);

        primaryStage.setTitle(Env.APP_NAME);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getResourceAsStream(ASSETS_PATH + "/telekit.png")));
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    public static InputStream getResourceAsStream(String resource) {
        return Objects.requireNonNull(DemoLauncher.class.getResourceAsStream(resource));
    }
}
