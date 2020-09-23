package org.telekit.ui.main;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.telekit.base.EventBus;
import org.telekit.base.fx.Controller;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.preferences.Language;
import org.telekit.ui.domain.ApplicationEvent;

import javax.inject.Inject;
import java.util.Arrays;

import static org.telekit.ui.domain.ApplicationEvent.Type.PREFERENCES_CHANGED;

public class PreferencesController extends Controller {

    private ApplicationPreferences preferences;

    public @FXML GridPane rootPane;
    public @FXML ComboBox<Language> cmbLanguage;
    public @FXML CheckBox cbSystemTray;

    @Inject
    public PreferencesController(ApplicationPreferences preferences) {
        this.preferences = preferences;
    }

    @FXML
    public void initialize() {
        cmbLanguage.getItems().addAll(Language.values());
        cmbLanguage.setConverter(new StringConverter<>() {
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
        cmbLanguage.getSelectionModel().select(preferences.getLanguage());
        cbSystemTray.setSelected(preferences.isSystemTray());
    }

    @FXML
    public void apply() {
        preferences.setLanguage(cmbLanguage.getSelectionModel().getSelectedItem());
        preferences.setSystemTray(cbSystemTray.isSelected());

        rootPane.getScene().getWindow().hide();
        EventBus.getInstance().publish(new ApplicationEvent(PREFERENCES_CHANGED));
    }

    @FXML
    public void cancel() {
        rootPane.getScene().getWindow().hide();
    }

    @Override
    public void reset() {}
}
