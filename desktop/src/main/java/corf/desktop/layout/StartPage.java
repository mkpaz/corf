package corf.desktop.layout;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import corf.base.Env;
import corf.base.event.BrowseEvent;
import corf.base.event.Events;
import corf.base.desktop.ExtraStyles;

import java.net.URI;
import java.util.Objects;

import static atlantafx.base.theme.Styles.*;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class StartPage extends AnchorPane {

    public static final Image GITHUB_ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/github_ribbon.png").getResourceAsStream()
    ));

    public StartPage() {
        super();
        createView();
    }

    private void createView() {
        var version = new Text("v." + Env.getAppVersion());
        version.getStyleClass().add(TEXT_MUTED);

        var githubRibbon = new ImageView(GITHUB_ICON);
        githubRibbon.setOpacity(0.75);
        githubRibbon.setCursor(Cursor.HAND);
        githubRibbon.setOnMouseClicked(e -> Events.fire(new BrowseEvent(URI.create(Env.APP_PROJECT_PAGE))));
        AnchorPane.setTopAnchor(githubRibbon, 0d);
        AnchorPane.setRightAnchor(githubRibbon, 0d);

        var projectBox = new HBox(version);
        AnchorPane.setRightAnchor(projectBox, 20d);
        AnchorPane.setBottomAnchor(projectBox, 5d);

        // == CENTER ==

        var grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(createHotkeyDescription("open tool"), 0, 0);
        grid.add(createHotkeyCombination("CTRL + N"), 1, 0);
        grid.add(createHotkeyDescription("add tab"), 0, 1);
        grid.add(createHotkeyCombination("CTRL + T"), 1, 1);
        grid.add(createHotkeyDescription("close tab"), 0, 2);
        grid.add(createHotkeyCombination("CTRL + W"), 1, 2);

        grid.getColumnConstraints().setAll(
                new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.RIGHT, false),
                new ColumnConstraints(200, -1, -1, Priority.NEVER, HPos.LEFT, false)
        );

        // the value doesn't really matter, as content won't be shrunk
        // it just prevents GridPane from expanding and filling whole width and height
        grid.setMaxWidth(100);
        grid.setMaxHeight(100);

        var centerBox = new StackPane(grid);
        centerBox.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(centerBox, 20d);
        AnchorPane.setRightAnchor(centerBox, 20d);
        AnchorPane.setBottomAnchor(centerBox, 20d);
        AnchorPane.setLeftAnchor(centerBox, 20d);

        // == ROOT ==

        getChildren().addAll(centerBox, projectBox, githubRibbon);
        getStyleClass().addAll("start-page", ExtraStyles.BG_DEFAULT);
    }

    private Text createHotkeyDescription(String s) {
        var text = new Text(s);
        text.getStyleClass().addAll(TEXT, TEXT_SUBTLE);
        return text;
    }

    private Label createHotkeyCombination(String s) {
        var label = new Label(s);
        label.getStyleClass().addAll(ExtraStyles.MONOSPACE);
        label.setStyle("-fx-background-color:-color-bg-subtle;");
        label.setPadding(new Insets(5, 10, 5, 10));
        return label;
    }
}
