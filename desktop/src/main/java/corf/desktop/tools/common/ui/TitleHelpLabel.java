package corf.desktop.tools.common.ui;

import static atlantafx.base.theme.Styles.ACCENT;
import static atlantafx.base.theme.Styles.BUTTON_CIRCLE;
import static atlantafx.base.theme.Styles.FLAT;
import static atlantafx.base.theme.Styles.TITLE_4;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

public class TitleHelpLabel extends HBox {

    public static final int GRAPHIC_TEXT_GAP = 10;

    private final Button helpBtn;

    public TitleHelpLabel(String text) {
        super();

        var label = new Label(text);

        helpBtn = new Button("", new FontIcon(Material2OutlinedAL.LIVE_HELP));
        helpBtn.getStyleClass().addAll(BUTTON_CIRCLE, FLAT, ACCENT);
        helpBtn.setPadding(new Insets(2));
        helpBtn.setCursor(Cursor.HAND);

        setAlignment(Pos.BASELINE_LEFT);
        setSpacing(GRAPHIC_TEXT_GAP);
        getChildren().addAll(label, helpBtn);
        getStyleClass().add(TITLE_4);
    }

    public void setOnHelpButtonClicked(EventHandler<ActionEvent> handler) {
        helpBtn.setOnAction(handler);
    }
}
