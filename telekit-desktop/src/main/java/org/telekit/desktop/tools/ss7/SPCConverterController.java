package org.telekit.desktop.tools.ss7;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.domain.exception.InvalidInputException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.telecom.ss7.SignallingPointCode;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.util.BooleanBindings;

import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.telecom.ss7.SignallingPointCode.Format;
import static org.telekit.base.telecom.ss7.SignallingPointCode.Type;
import static org.telekit.desktop.MessageKeys.TOOLS_SS7_MSG_INVALID_POINT_CODE;

@FxmlPath("/org/telekit/desktop/tools/ss7/spc-conv.fxml")
public class SPCConverterController implements Component {

    private static final int NAME_PADDING = 16;

    public @FXML VBox rootPane;
    public @FXML ComboBox<Type> cmbType;
    public @FXML TextField tfSpc;
    public @FXML ComboBox<Format> cmbFormat;
    public @FXML TextArea taResult;
    public @FXML Button btnConvert;

    @FXML
    public void initialize() {
        cmbType.getItems().addAll(Type.values());
        cmbType.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) onTypeChanged(newVal);
        });

        cmbFormat.setButtonCell(new SPCFormatCell());
        cmbFormat.setCellFactory(property -> new SPCFormatCell());

        tfSpc.setOnKeyPressed(keyCode -> {
            if (keyCode.getCode().equals(KeyCode.ENTER) && isNotBlank(tfSpc.getText())) {
                convert();
            }
        });
        btnConvert.disableProperty().bind(BooleanBindings.isBlank(tfSpc.textProperty()));

        cmbType.getSelectionModel().select(Type.ITU);
        cmbFormat.getItems().setAll(Type.ITU.formats());
        cmbFormat.getSelectionModel().selectFirst();
    }

    @FXML
    public void onTypeChanged(Type type) {
        cmbFormat.getItems().setAll(type.formats());
        cmbFormat.getSelectionModel().selectFirst();
    }

    @FXML
    public void convert() {
        String spcStr = trim(tfSpc.getText());
        Type type = cmbType.getSelectionModel().getSelectedItem();
        Format fmt = cmbFormat.getSelectionModel().getSelectedItem();

        if (isEmpty(spcStr) || fmt == null) return;

        try {
            SignallingPointCode spc = SignallingPointCode.parse(spcStr, type, fmt);
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

        if (spc.getLength() == Type.ITU.getBitLength()) {
            text.appendLine(pad("ITU [3-8-3]:"), spc.toString(Format.STRUCT_383));
            text.appendLine(pad("RUS [8-6]:"), spc.toString(Format.STRUCT_86));
        }

        if (spc.getLength() == Type.ANSI.getBitLength()) {
            text.appendLine(pad("ANSI [8-8-8]:"), spc.toString(Format.STRUCT_888));
        }

        taResult.setText(text.toString());
    }

    private static String pad(String name) {
        return rightPad(name, NAME_PADDING);
    }

    @Override
    public Region getRoot() {
        return rootPane;
    }

    @Override
    public void reset() {}

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
