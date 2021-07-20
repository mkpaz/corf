package org.telekit.desktop.tools.ipcalc;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.telecom.IPv4AddressWrapper;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.TextFormatters;
import org.telekit.controls.widgets.OverlayDialog;

import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.util.CommonUtils.map;
import static org.telekit.controls.i18n.ControlsMessages.ACTION_OK;
import static org.telekit.controls.i18n.ControlsMessages.IP_ADDRESS;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.controls.util.Controls.gridLabel;
import static org.telekit.desktop.i18n.DesktopMessages.IPCALC_IP_ADDRESS_CONVERTER;

public class IPv4ConverterDialog extends OverlayDialog {

    TextField octetIPText;
    TextField binaryIPText;
    TextField hexIPText;
    TextField integerIPText;
    Button commitBtn;

    private Consumer<String> onCommitCallback;

    public IPv4ConverterDialog() {
        super();
        createContent();
    }

    private void createContent() {
        octetIPText = new TextField();
        octetIPText.setTextFormatter(TextFormatters.ipv4Decimal());
        octetIPText.textProperty().addListener((obs, old, value) -> updateAll(parseIPAddress(value, -1)));

        binaryIPText = new TextField();
        binaryIPText.textProperty().addListener((obs, old, value) -> updateAll(parseIPAddress(value, 2)));

        hexIPText = new TextField();
        hexIPText.textProperty().addListener((obs, old, value) -> updateAll(parseIPAddress(value, 16)));

        integerIPText = new TextField();
        integerIPText.textProperty().addListener((obs, old, value) -> updateAll(parseIPAddress(value, 10)));

        // GRID

        GridPane grid = Containers.gridPane(20, 10, new Insets(10));

        grid.add(gridLabel(t(IP_ADDRESS), HPos.RIGHT, octetIPText), 0, 0);
        grid.add(octetIPText, 1, 0);

        grid.add(gridLabel("Binary", HPos.RIGHT, binaryIPText), 0, 1);
        grid.add(binaryIPText, 1, 1);

        grid.add(gridLabel("Hex", HPos.RIGHT, hexIPText), 0, 2);
        grid.add(hexIPText, 1, 2);

        grid.add(gridLabel("Integer", HPos.RIGHT, integerIPText), 0, 3);
        grid.add(integerIPText, 1, 3);

        grid.getColumnConstraints().addAll(columnConstraints(80, Priority.SOMETIMES), HGROW_ALWAYS);
        grid.getRowConstraints().addAll(VGROW_NEVER, VGROW_NEVER, VGROW_NEVER, VGROW_NEVER);

        // ~

        commitBtn = button(t(ACTION_OK), null, "form-action");
        commitBtn.setDefaultButton(true);
        commitBtn.setOnAction(e -> commit());

        footerBox.getChildren().add(1, commitBtn);
        setPrefWidth(500);
        setTitle(t(IPCALC_IP_ADDRESS_CONVERTER));

        setContent(grid);
    }

    public void setData(String ipStr) {
        if (IPv4AddressWrapper.isValidString(ipStr)) {
            octetIPText.setText(ipStr);
        } else {
            octetIPText.setText(IPv4CalcViewModel.DEFAULT_IP);
        }
    }

    public void setOnCommit(Consumer<String> handler) {
        this.onCommitCallback = handler;
    }

    private void commit() {
        if (onCommitCallback != null) { onCommitCallback.accept(octetIPText.getText()); }
    }

    private @Nullable IPv4AddressWrapper parseIPAddress(String ip, int radix) {
        if (isBlank(ip)) { return null; }

        try {
            // parse from octet string
            if (radix == -1) { return new IPv4AddressWrapper(ip); }

            // parse from number
            long longValue = Long.parseLong(ip, radix);
            if (longValue < IPv4AddressWrapper.MIN_VALUE || longValue > IPv4AddressWrapper.MAX_VALUE) { return null; }

            return new IPv4AddressWrapper((int) longValue);
        } catch (Throwable e) {
            return null;
        }
    }

    private void updateOne(TextField textField, String value) {
        // don't update focused field, because user edits it in that very moment
        if (textField.isFocused()) { return; }
        Platform.runLater(() -> textField.setText(value));
    }

    private void updateAll(IPv4AddressWrapper ip) {
        updateOne(octetIPText, map(ip, IPv4AddressWrapper::toString, ""));
        updateOne(binaryIPText, map(ip, IPv4AddressWrapper::toBinaryString, ""));
        updateOne(hexIPText, map(ip, IPv4AddressWrapper::toHexString, ""));
        updateOne(integerIPText, map(ip, val -> String.valueOf(val.longValue()), ""));
    }
}
