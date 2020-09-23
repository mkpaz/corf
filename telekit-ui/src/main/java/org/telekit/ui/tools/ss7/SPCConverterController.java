package org.telekit.ui.tools.ss7;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.telekit.base.i18n.Messages;
import org.telekit.base.fx.Controller;
import org.telekit.base.fx.FXBindings;
import org.telekit.base.util.TextBuilder;

import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.util.telecom.SS7Utils.*;
import static org.telekit.ui.main.MessageKeys.TOOLS_SS7_MSG_INVALID_POINT_CODE;

public class SPCConverterController extends Controller {

    public @FXML TextField tfSpc;
    public @FXML ComboBox<SPCFormat> cmbFormat;
    public @FXML ToggleGroup toggleType;
    public @FXML RadioButton rb14bit;
    public @FXML RadioButton rb24bit;
    public @FXML TextArea taResult;
    public @FXML Button btnConvert;

    @FXML
    public void initialize() {
        toggleType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) onTypeChanged(newValue);
        });
        rb14bit.setUserData(14);
        rb24bit.setUserData(24);
        rb14bit.setSelected(true);

        cmbFormat.setButtonCell(new SPCFormatCell());
        cmbFormat.setCellFactory(property -> new SPCFormatCell());
        cmbFormat.getItems().setAll(FORMATS_14_BIT);
        cmbFormat.getSelectionModel().selectFirst();

        btnConvert.disableProperty().bind(FXBindings.isBlank(tfSpc.textProperty()));
    }

    @FXML
    public void onTypeChanged(Toggle toggle) {
        if ((Integer) toggle.getUserData() == 14) cmbFormat.getItems().setAll(FORMATS_14_BIT);
        if ((Integer) toggle.getUserData() == 24) cmbFormat.getItems().setAll(FORMATS_24_BIT);
        cmbFormat.getSelectionModel().selectFirst();
    }

    @FXML
    public void convert() {
        String spcStr = trim(tfSpc.getText());
        SPCFormat format = cmbFormat.getSelectionModel().getSelectedItem();
        int length = (Integer) toggleType.getSelectedToggle().getUserData();

        if (isEmpty(spcStr) || format == null) return;

        int spc = parsePointCode(spcStr, format, length);

        if (spc > 0) {
            updateResult(spc, length);
        } else {
            taResult.setText(Messages.get(TOOLS_SS7_MSG_INVALID_POINT_CODE));
        }
    }

    public void updateResult(int spc, int length) {
        TextBuilder tb = new TextBuilder();
        int padding = 16;

        tb.appendLine(rightPad("DEC: ", padding), formatPointCode(spc, length, SPCFormat.DEC));
        tb.appendLine(rightPad("HEX: ", padding), formatPointCode(spc, length, SPCFormat.HEX));
        tb.appendLine(rightPad("BIN: ", padding), formatPointCode(spc, length, SPCFormat.BIN));

        if (length == 14) {
            tb.appendLine(rightPad("ITU [3-8-3]: ", padding), formatPointCode(spc, length, SPCFormat.STRUCT_383));
            tb.appendLine(rightPad("RUS [8-6]: ", padding), formatPointCode(spc, length, SPCFormat.STRUCT_86));
        }

        if (length == 24) {
            tb.appendLine(rightPad("ANSI [8-8-8]: ", padding), formatPointCode(spc, length, SPCFormat.STRUCT_888));
        }

        taResult.setText(tb.toString());
    }

    @Override
    public void reset() {}

    ///////////////////////////////////////////////////////////////////////////

    private static class SPCFormatCell extends ListCell<SPCFormat> {

        @Override
        protected void updateItem(SPCFormat spcFormat, boolean empty) {
            super.updateItem(spcFormat, empty);

            if (spcFormat != null) {
                setText(spcFormat.getDescription());
            } else {
                setText(null);
            }
        }
    }
}
