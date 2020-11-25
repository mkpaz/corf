package org.telekit.ui.tools.ss7;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.telekit.base.domain.exception.InvalidInputException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.telecom.ss7.SignallingPointCode;
import org.telekit.base.ui.Controller;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.util.BooleanBindings;

import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.telecom.ss7.SignallingPointCode.*;
import static org.telekit.ui.MessageKeys.TOOLS_SS7_MSG_INVALID_POINT_CODE;

public class SPCConverterController extends Controller {

    private static final int NAME_PADDING = 16;

    public @FXML TextField tfSpc;
    public @FXML ComboBox<Format> cmbFormat;
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
        rb14bit.setUserData(LEN_ITU);
        rb24bit.setUserData(LEN_ANSI);
        rb14bit.setSelected(true);

        cmbFormat.setButtonCell(new SPCFormatCell());
        cmbFormat.setCellFactory(property -> new SPCFormatCell());
        cmbFormat.getItems().setAll(FORMATS_14_BIT);
        cmbFormat.getSelectionModel().selectFirst();

        btnConvert.disableProperty().bind(BooleanBindings.isBlank(tfSpc.textProperty()));
    }

    @FXML
    public void onTypeChanged(Toggle toggle) {
        if ((Integer) toggle.getUserData() == LEN_ITU) cmbFormat.getItems().setAll(FORMATS_14_BIT);
        if ((Integer) toggle.getUserData() == LEN_ANSI) cmbFormat.getItems().setAll(FORMATS_24_BIT);
        cmbFormat.getSelectionModel().selectFirst();
    }

    @FXML
    public void convert() {
        String spcStr = trim(tfSpc.getText());
        Format fmt = cmbFormat.getSelectionModel().getSelectedItem();
        int length = (Integer) toggleType.getSelectedToggle().getUserData();

        if (isEmpty(spcStr) || fmt == null) return;

        try {
            SignallingPointCode spc = SignallingPointCode.parse(spcStr, fmt, length);
            updateResult(spc);
        } catch (InvalidInputException e) {
            taResult.setText(Messages.get(TOOLS_SS7_MSG_INVALID_POINT_CODE));
        }
    }

    public void updateResult(SignallingPointCode spc) {
        TextBuilder text = new TextBuilder();

        text.appendLine(pad("DEC:"), spc.toString(Format.DEC));
        text.appendLine(pad("HEX:"), spc.toString(Format.HEX));
        text.appendLine(pad("BIN:"), spc.toString(Format.BIN));

        if (spc.getLength() == LEN_ITU) {
            text.appendLine(pad("ITU [3-8-3]:"), spc.toString(Format.STRUCT_383));
            text.appendLine(pad("RUS [8-6]:"), spc.toString(Format.STRUCT_86));
        }

        if (spc.getLength() == LEN_ANSI) {
            text.appendLine(pad("ANSI [8-8-8]:"), spc.toString(Format.STRUCT_888));
        }

        taResult.setText(text.toString());
    }

    private static String pad(String name) {
        return rightPad(name, NAME_PADDING);
    }

    private static class SPCFormatCell extends ListCell<Format> {

        @Override
        protected void updateItem(Format format, boolean empty) {
            super.updateItem(format, empty);

            if (format != null) {
                setText(format.getDescription());
            } else {
                setText(null);
            }
        }
    }
}
