package org.telekit.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.telekit.base.ApplicationContext;
import org.telekit.base.Env;
import org.telekit.base.i18n.BaseMessagesBundleProvider;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Tool;
import org.telekit.base.ui.IconCache;
import org.telekit.base.ui.UIDefaults;
import org.telekit.example.ExamplePlugin;
import org.telekit.example.service.ExampleDependencyModule;
import org.telekit.example.tools.ExampleController;
import org.telekit.example.tools.HelloTool;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.example.ExamplePlugin.ASSETS_PATH;

public class Launcher extends Application implements UIDefaults {

    private final ApplicationContext applicationContext = ApplicationContext.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        applicationContext.configure(new ExampleDependencyModule());

        ExamplePlugin plugin = new ExamplePlugin();

        Messages.getInstance().load(
                BaseMessagesBundleProvider.getBundle(Locale.getDefault()), Messages.class.getName()
        );
        Messages.getInstance().load(
                plugin.getBundle(Locale.getDefault()), Launcher.class.getName()
        );

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace());
        IconCache.put(ICON_APP, new Image(getResourceAsStream(ASSETS_PATH + "images/telekit.png")));

        Tool exampleTool = new HelloTool();
        ExampleController controller = (ExampleController) exampleTool.createController();

        primaryStage.setTitle(Env.APP_NAME);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(controller.getParent(), PREF_WIDTH, PREF_HEIGHT));
        primaryStage.getIcons().add(IconCache.get(ICON_APP));
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }

    public static InputStream getResourceAsStream(String resource) {
        return Objects.requireNonNull(Launcher.class.getResourceAsStream(resource));
    }
}
