package org.telekit.desktop.tools.translit;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.service.Transliterator;
import org.telekit.base.service.impl.RUTransliterator;
import org.telekit.controls.util.BindUtils;

import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.trim;

@FxmlPath("/org/telekit/desktop/tools/translit/_root.fxml")
public class TranslitController implements Component {

    private static final List<String> SUPPORTED_LANGUAGES = List.of("RU");

    public @FXML ComboBox<String> cmbLang;
    public @FXML Button btnTransliterate;
    public @FXML TextArea taText;
    public @FXML TextArea taTransliteratedText;
    public @FXML GridPane rootPane;

    @FXML
    public void initialize() {
        cmbLang.setItems(observableArrayList(SUPPORTED_LANGUAGES));
        cmbLang.getSelectionModel().selectFirst();
        btnTransliterate.disableProperty().bind(BindUtils.isBlank(taText.textProperty()));
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

    @Override
    public Region getRoot() {
        return rootPane;
    }

    @Override
    public void reset() {}
}
