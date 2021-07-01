package org.telekit.desktop.views.system;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.telekit.base.preferences.Language;
import org.telekit.controls.custom.RevealablePasswordField;
import org.telekit.controls.custom.ToggleIcon;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.desktop.i18n.DesktopMessages;

import java.util.Arrays;

import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.gridLabel;
import static org.telekit.desktop.views.system.PreferencesView.RESTART_MARK;

public class GeneralPreferencesTab extends Tab {

    ComboBox<Language> langChoice;
    TextField proxyUrlText;
    TextField proxyUsernameText;

    RevealablePasswordField proxyPasswordText;
    ToggleIcon proxyPasswordToggle;
//    TextField proxyPasswordText;

    private final PreferencesViewModel model;

    public GeneralPreferencesTab(PreferencesViewModel model) {
        this.model = model;

        createView();
    }

    private void createView() {
        langChoice = new ComboBox<>();
        langChoice.getItems().addAll(Language.values());
        langChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Language lang) {
                return lang.getDisplayName();
            }

            @Override
            public Language fromString(String displayName) {
                return Arrays.stream(Language.values())
                        .filter(lang -> lang.getDisplayName().equals(displayName))
                        .findFirst()
                        .orElse(Language.EN);
            }
        });
        langChoice.valueProperty().bindBidirectional(model.languageProperty());

        HBox proxyGroupHeader = Containers.create(HBox::new, "group-header");
        proxyGroupHeader.setAlignment(Pos.BASELINE_LEFT);
        proxyGroupHeader.getChildren().addAll(new Label(t(DesktopMessages.PREFERENCES_PROXY)), horizontalSeparator());

        proxyUrlText = new TextField();
        proxyUrlText.textProperty().bindBidirectional(model.proxyUrlProperty());

        proxyUsernameText = new TextField();
        proxyUsernameText.textProperty().bindBidirectional(model.proxyUsernameProperty());

        proxyPasswordText = Controls.passwordField();
        proxyPasswordText.textProperty().bindBidirectional(model.proxyPasswordProperty());

        // GRID

        GridPane grid = gridPane(20, 10, new Insets(10), "grid");

        grid.add(gridLabel(RESTART_MARK + t(DesktopMessages.LANGUAGE), HPos.RIGHT, langChoice), 0, 0, 2, 1);
        grid.add(langChoice, 2, 0);

        grid.add(proxyGroupHeader, 0, 1, GridPane.REMAINING, 1);

        grid.add(gridLabel("URL", HPos.RIGHT, proxyUrlText), 1, 2);
        grid.add(proxyUrlText, 2, 2);

        grid.add(gridLabel(t(DesktopMessages.USERNAME), HPos.RIGHT, proxyUsernameText), 1, 3);
        grid.add(proxyUsernameText, 2, 3);

        grid.add(gridLabel(t(DesktopMessages.PASSWORD), HPos.RIGHT, proxyPasswordText), 1, 4);
        grid.add(proxyPasswordText.getParent(), 2, 4);

        grid.getColumnConstraints().addAll(
                columnConstraints(10, Priority.NEVER),  // imitates padding for nested properties
                columnConstraints(80, Priority.NEVER),  // property name
                columnConstraints(Priority.ALWAYS)              // property value
        );

        setText(t(DesktopMessages.PREFERENCES_GENERAL));
        setContent(grid);
    }
}
