package org.telekit.ui.tools.ip_calculator;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.ui.Controller;
import org.telekit.controls.format.TextFormatters;
import org.telekit.base.telecom.net.IP4Address;

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
        tfIPCanonical.textProperty().addListener(
                (observable, oldValue, newValue) -> updateAll(parseIP(newValue, -1))
        );

        tfIPBinary.textProperty().addListener(
                (observable, oldValue, newValue) -> updateAll(parseIP(removeSeparators(newValue), 2))
        );

        tfIPHex.textProperty().addListener(
                (observable, oldValue, newValue) -> updateAll(parseIP(removeSeparators(newValue), 16))
        );

        tfIPInteger.textProperty().addListener(
                (observable, oldValue, newValue) -> updateAll(parseIP(newValue, 10))
        );
    }

    public void setData(long ip) {
        tfIPInteger.setText(String.valueOf(ip)); // should trigger auto-update on all other fields
    }

    @Nullable
    private IP4Address parseIP(String value, int radix) {
        if (isBlank(value)) return null;
        try {
            // parse from octet string
            if (radix == -1) {
                return new IP4Address(value);
            }

            // parse from number
            long longValue = Long.parseLong(value, radix);
            if (longValue < IP4Address.MIN_VALUE || longValue > IP4Address.MAX_VALUE) return null;
            return new IP4Address((int) longValue);
        } catch (Throwable e) {
            return null;
        }
    }

    private void updateAll(IP4Address ip) {
        updateOne(tfIPCanonical, nullAwareConversion(
                ip, IP4Address::toString)
        );
        updateOne(tfIPBinary, nullAwareConversion(
                ip, value -> formatBinaryIP(value.toBinaryString())
        ));
        updateOne(tfIPHex, nullAwareConversion(
                ip, value -> formatHexIP(value.toHexString())
        ));
        updateOne(tfIPInteger, nullAwareConversion(
                ip, value -> String.valueOf(value.longValue())
        ));
    }

    private void updateOne(final TextField tf, final String value) {
        if (tf.isFocused()) return; // don't update focused field, because user edits it in that moment
        Platform.runLater(() -> tf.setText(value));
    }

    private String nullAwareConversion(IP4Address ip, Function<IP4Address, String> converter) {
        if (ip == null) return "";
        return converter.apply(ip);
    }

    private String removeSeparators(String value) {
        if (value == null) return null;
        return value.replaceAll("[.]", "");
    }

    @FXML
    public void switchBinaryFormat() {
        tfIPBinary.setText(formatBinaryIP(tfIPBinary.getText()));
    }

    private String formatBinaryIP(String value) {
        if (cbIPBinaryDotted.isSelected()) {
            return String.join(".", splitEqually(leftPad(value, 32, "0"), 8));
        } else {
            return removeSeparators(value);
        }
    }

    @FXML
    public void switchHexFormat() {
        tfIPHex.setText(formatHexIP(tfIPHex.getText()));
    }

    private String formatHexIP(String value) {
        if (cbIPHexDotted.isSelected()) {
            return String.join(".", splitEqually(leftPad(value, 8, "0"), 2));
        } else {
            return removeSeparators(value);
        }
    }

    @FXML
    public void close() {
        rootPane.getScene().getWindow().hide();
    }

    @Override
    public void reset() {}
}
