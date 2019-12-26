package org.telekit.ui.tools.transliterator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.telekit.base.fx.Controller;
import org.telekit.base.fx.FXBindings;
import org.telekit.base.util.tramsliterate.RUTransliterator;
import org.telekit.base.util.tramsliterate.Transliterator;

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

        btnTransliterate.disableProperty().bind(
                FXBindings.isBlank(taText.textProperty())
        );
    }

    @FXML
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public void transliterate() {
        String lang = cmbLang.getSelectionModel().getSelectedItem();
        String text = trim(taText.getText());

        Transliterator tr = null;
        switch (lang) {
            case "RU":
                tr = new RUTransliterator();
        }

        if (tr != null) {
            taTransliteratedText.setText(tr.transliterate(text));
        }
    }

    @Override
    public void reset() {}
}
