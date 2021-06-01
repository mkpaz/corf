package org.telekit.desktop.tools.ipcalc;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.event.CancelEvent;
import org.telekit.base.telecom.ip.IP4Address;
import org.telekit.base.ui.Controller;
import org.telekit.controls.format.TextFormatters;

import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.telekit.base.util.StringUtils.splitEqually;

public class IPv4ConverterController extends Controller {

    public @FXML GridPane rootPane;
    public @FXML TextField tfIPCanonical;
    public @FXML TextField tfIPBinary;
    public @FXML TextField tfIPHex;
    public @FXML TextField tfIPInteger;
    public @FXML CheckBox cbIPBinaryDotted;
    public @FXML CheckBox cbIPHexDotted;

    @FXML
    public void initialize() {
        tfIPCanonical.setTextFormatter(TextFormatters.ipv4Decimal());

        // cleanup removes octet separators before parsing, which is only needed for binary and hex formats
        // because of parseLong() method
        tfIPCanonical.textProperty()
                .addListener((obs, oldVal, newVal) -> updateAll(parseIPAddress(newVal, -1)));
        tfIPBinary.textProperty()
                .addListener((obs, oldVal, newVal) -> updateAll(parseIPAddress(cleanup(newVal), 2)));
        tfIPHex.textProperty()
                .addListener((obs, oldVal, newVal) -> updateAll(parseIPAddress(cleanup(newVal), 16)));
        tfIPInteger.textProperty()
                .addListener((obs, oldVal, newVal) -> updateAll(parseIPAddress(newVal, 10)));
    }

    public void setData(long ip) {
        // only set one value, it should trigger auto-update on all other fields
        tfIPInteger.setText(String.valueOf(ip));
    }

    private @Nullable IP4Address parseIPAddress(String ipaddr, int radix) {
        if (isBlank(ipaddr)) return null;
        try {
            // parse from octet string
            if (radix == -1) return new IP4Address(ipaddr);

            // parse from number (remove octet separators before parsing)
            long longValue = Long.parseLong(ipaddr, radix);
            if (longValue < IP4Address.MIN_VALUE || longValue > IP4Address.MAX_VALUE) return null;

            return new IP4Address((int) longValue);
        } catch (Throwable e) {
            return null;
        }
    }

    private void update(TextField textField, String value) {
        // don't update focused field, because user edits it in that very moment
        if (textField.isFocused()) return;
        Platform.runLater(() -> textField.setText(value));
    }

    private void updateAll(IP4Address ip) {
        update(tfIPCanonical, ensureNotNull(ip, IP4Address::toString));
        update(tfIPBinary, ensureNotNull(ip, val -> formatBinaryIP(val.toBinaryString())));
        update(tfIPHex, ensureNotNull(ip, val -> formatHexIP(val.toHexString())));
        update(tfIPInteger, ensureNotNull(ip, val -> String.valueOf(val.longValue())));
    }

    @FXML
    public void switchBinaryFormat() {
        tfIPBinary.setText(formatBinaryIP(tfIPBinary.getText()));
    }

    @FXML
    public void switchHexFormat() {
        tfIPHex.setText(formatHexIP(tfIPHex.getText()));
    }

    @FXML
    public void close() {
        eventBus.publish(new CancelEvent());
    }

    private static String ensureNotNull(IP4Address ip, Function<IP4Address, String> converter) {
        if (ip == null) return "";
        return converter.apply(ip);
    }

    private static String cleanup(String str) {
        if (str == null) return null;
        return str.replaceAll("[.]", "");
    }

    private String formatBinaryIP(String value) {
        if (cbIPBinaryDotted.isSelected()) {
            return String.join(".", splitEqually(leftPad(value, 32, "0"), 8));
        } else {
            return cleanup(value);
        }
    }

    private String formatHexIP(String value) {
        if (cbIPHexDotted.isSelected()) {
            return String.join(".", splitEqually(leftPad(value, 8, "0"), 2));
        } else {
            return cleanup(value);
        }
    }
}
