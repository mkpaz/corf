package telekit.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import telekit.base.Env;
import telekit.base.desktop.ViewLoader;
import telekit.base.di.Injector;
import telekit.base.i18n.BaseMessages;
import telekit.base.i18n.I18n;
import telekit.base.plugin.Tool;
import telekit.example.ExamplePlugin;
import telekit.example.service.ExampleDependencyModule;
import telekit.example.tools.ExampleController;
import telekit.example.tools.HelloTool;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static telekit.example.ExamplePlugin.ASSETS_PATH;

public class DemoLauncher extends Application {

    private final Injector injector = Injector.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        injector.configure(new ExampleDependencyModule());

        ExamplePlugin plugin = new ExamplePlugin();

        Locale.setDefault(defaultIfNull(Env.LOCALE, Locale.getDefault()));
        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().register(plugin.getBundleLoader());
        I18n.getInstance().reload();

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());

        Tool<ExampleController> tool = new HelloTool();
        ExampleController controller = ViewLoader.load(tool.getComponent());
        Scene scene = new Scene(controller.getRoot(), 800, 600);

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
