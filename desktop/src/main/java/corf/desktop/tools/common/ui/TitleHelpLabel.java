package corf.desktop.tools.common.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import static atlantafx.base.theme.Styles.*;

public class TitleHelpLabel extends Label {

    private final Button helpBtn;

    public TitleHelpLabel(String text) {
        super(text);

        helpBtn = new Button("", new FontIcon(Material2OutlinedAL.LIVE_HELP));
        helpBtn.getStyleClass().addAll(BUTTON_CIRCLE, FLAT, ACCENT);
        helpBtn.setPadding(new Insets(2));
        helpBtn.setCursor(Cursor.HAND);

        getStyleClass().add(TITLE_4);
        setContentDisplay(ContentDisplay.RIGHT);
        setGraphic(helpBtn);
        setGraphicTextGap(10);
    }

    public void setOnHelpButtonClicked(EventHandler<ActionEvent> handler) {
        helpBtn.setOnAction(handler);
    }
}
