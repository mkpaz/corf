package org.telekit.base.fx;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public final class FXFormatters {

    public static TextFormatter<String> ipv4Decimal() {
        String regex = createIPv4DecimalRegex();
        final UnaryOperator<TextFormatter.Change> ipAddressFilter = change -> {
            String text = change.getControlNewText();
            if (text.matches(regex)) {
                return change;
            } else {
                return null;
            }
        };
        return new TextFormatter<>(ipAddressFilter);
    }

    private static String createIPv4DecimalRegex() {
        String octet = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentOctet = "(\\." + octet + ")";
        return "^" + octet + "?" + subsequentOctet + "{0,3}";
    }
}
