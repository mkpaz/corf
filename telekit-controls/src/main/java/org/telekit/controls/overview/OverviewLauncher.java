package org.telekit.controls.overview;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.i18n.BaseMessagesBundleProvider;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.UIDefaults;
import org.telekit.base.ui.UILoader;
import org.telekit.controls.theme.ThemeLoader;
import org.telekit.controls.i18n.ControlsMessagesBundleProvider;

public class OverviewLauncher extends Application implements UIDefaults {

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

        Controller controller = UILoader.load(
                ThemeLoader.getResource("/assets/overview/sampler.fxml"),
                Messages.getInstance()
        );

        // create scene and apply (TBD: user selected) theme to it
        Scene scene = new Scene(controller.getParent(), 1024, 768);
        ThemeLoader themeLoader = new ThemeLoader();
        scene.getStylesheets().addAll(themeLoader.getStylesheets(""));

        primaryStage.setTitle("Components Overview");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> Platform.exit());
        primaryStage.show();
    }
}