package org.telekit.desktop.tools.translit;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.telekit.base.service.Transliterator;
import org.telekit.base.service.impl.RUTransliterator;
import org.telekit.base.ui.Controller;
import org.telekit.controls.util.BooleanBindings;

import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.trim;

public class RootController extends Controller {

    private static final List<String> SUPPORTED_LANGUAGES = List.of("RU");

    public @FXML ComboBox<String> cmbLang;
    public @FXML Button btnTransliterate;
    public @FXML TextArea taText;
    public @FXML TextArea taTransliteratedText;

    @FXML
    public void initialize() {
        cmbLang.setItems(observableArrayList(SUPPORTED_LANGUAGES));
        cmbLang.getSelectionModel().selectFirst();
        btnTransliterate.disableProperty().bind(BooleanBindings.isBlank(taText.textProperty()));
    }

    @FXML
    public void transliterate() {
        String lang = cmbLang.getSelectionModel().getSelectedItem();
        String text = trim(taText.getText());

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        Transliterator exec = switch (lang) {
            case "RU" -> new RUTransliterator();
            default -> null;
        };

        if (exec != null) {
            taTransliteratedText.setText(exec.transliterate(text));
        }
    }
}
