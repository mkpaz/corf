package telekit.desktop.views.system;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import telekit.base.preferences.internal.Language;
import telekit.desktop.i18n.DesktopMessages;

import java.util.Arrays;

import static telekit.base.i18n.I18n.t;
import static telekit.controls.util.Containers.columnConstraints;
import static telekit.controls.util.Containers.gridPane;
import static telekit.controls.util.Controls.gridLabel;
import static telekit.desktop.views.system.PreferencesView.RESTART_MARK;

public class GeneralPreferencesTab extends Tab {

    ComboBox<Language> langChoice;

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

        // GRID

        GridPane grid = gridPane(20, 10, new Insets(10), "grid");

        grid.add(gridLabel(RESTART_MARK + t(DesktopMessages.LANGUAGE), HPos.RIGHT, langChoice), 0, 0);
        grid.add(langChoice, 1, 0);

        grid.getColumnConstraints().addAll(
                columnConstraints(120, Priority.NEVER),  // property name
                columnConstraints(Priority.ALWAYS)              // property value
        );

        setText(t(DesktopMessages.PREFERENCES_GENERAL));
        setContent(grid);
    }
}
