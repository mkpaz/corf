package org.telekit.controls.format;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class TextFormatters {

    public static TextFormatter<String> matches(Pattern pattern) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        };
        return new TextFormatter<>(filter);
    }

    public static TextFormatter<String> ipv4Decimal() {
        return matches(Pattern.compile(createIPv4DecimalRegex()));
    }

    // TODO: move to Patterns class
    private static String createIPv4DecimalRegex() {
        String octet = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentOctet = "(\\." + octet + ")";
        return "^" + octet + "?" + subsequentOctet + "{0,3}";
    }
}
