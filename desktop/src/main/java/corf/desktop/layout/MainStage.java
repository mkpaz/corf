package corf.desktop.layout;

import atlantafx.base.theme.Theme;
import corf.base.Env;
import corf.base.desktop.Dimension;
import corf.base.event.Events;
import corf.base.event.ThemeEvent;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.desktop.ResourceLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

import static corf.base.Env.APP_NAME;
import static corf.base.Env.FULLSCREEN_SIZE;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class MainStage {

    public static final Dimension MIN_SIZE = new Dimension(800, 600);
    public static final Dimension PREF_SIZE = new Dimension(1280, 800);
    public static final PseudoClass DARK = PseudoClass.getPseudoClass("dark");

    final Stage stage;
    final Scene scene;

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();

    private MainStage(Stage primaryStage, Scene scene, Theme theme) {
        this.stage = Objects.requireNonNull(primaryStage, "primaryStage");
        this.scene = Objects.requireNonNull(scene, "scene");
        this.theme.set(Objects.requireNonNull(theme, "theme"));

        stage.setScene(scene);
        stage.setOnCloseRequest(t -> Platform.exit());
        stage.setMinWidth(MIN_SIZE.width());
        stage.setMinHeight(MIN_SIZE.height());
        stage.setTitle(APP_NAME);
        stage.getIcons().add(Env.APP_ICON);

        // set user agent stylesheet
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());

        // application styles
        scene.getStylesheets().addAll(new ResourceLoader(DESKTOP_MODULE.getAnchorClass()).require(
                DESKTOP_MODULE.concat("assets/styles/index.css").toString()
        ));
        scene.getStylesheets().addAll(new ResourceLoader(Env.BASE_MODULE.getAnchorClass()).require(
                Env.BASE_MODULE.concat("assets/fonts.css").toString(),
                Env.BASE_MODULE.concat("assets/index.css").toString()
        ));

        init();
    }

    private void init() {
        theme.addListener((obs, old, val) -> {
            if (val != null) {
                Application.setUserAgentStylesheet(val.getUserAgentStylesheet());
                scene.getRoot().pseudoClassStateChanged(DARK, val.isDarkMode());
                Events.fire(new ThemeEvent(val));
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    public Scene getScene() {
        return scene;
    }

    public Theme getTheme() {
        return theme.get();
    }

    public void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    public Parent getContent() {
        return scene.getRoot();
    }

    public void setContent(Parent parent) {
        scene.setRoot(parent);
        parent.pseudoClassStateChanged(DARK, theme.get().isDarkMode());
    }

    public void setTitle(String title) {
        stage.setTitle(Objects.requireNonNullElse(title, APP_NAME));
    }

    public void show() {
        stage.show();
    }

    public static MainStage create(Stage primaryStage, ApplicationPreferences preferences) {
        Dimension initialSize = getSceneSize(preferences);
        var scene = new DefaultScene(initialSize.width(), initialSize.height());
        var mainStage = new MainStage(primaryStage, scene, preferences.getStyleTheme());

        mainStage.getStage().setOnCloseRequest(t -> {
            preferences.getSystemPreferences().setMainWindowSize(Dimension.of(mainStage.getStage()));
            Platform.exit();
        });

        if (FULLSCREEN_SIZE.equals(Dimension.of(scene))) {
            mainStage.getStage().setMaximized(true);
        }

        return mainStage;
    }

    private static Dimension getSceneSize(ApplicationPreferences preferences) {
        // use last remembered window size
        Dimension storedSize = preferences.getSystemPreferences().getMainWindowSize();
        if (storedSize != null) { return storedSize; }

        Dimension visualBounds = Dimension.of(Screen.getPrimary().getVisualBounds());

        // make sure that window size less than visual screen bounds
        // the latter is calculated based on display resolution, output scale and taskbar height
        return PREF_SIZE.gt(visualBounds) ? visualBounds : PREF_SIZE;
    }
}
