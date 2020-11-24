package org.telekit.controls.format;

import javafx.scene.control.TextFormatter;
import org.telekit.base.telecom.net.IP4Address;

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
        return matches(Pattern.compile(IP4Address.PATTERN));
    }
}
