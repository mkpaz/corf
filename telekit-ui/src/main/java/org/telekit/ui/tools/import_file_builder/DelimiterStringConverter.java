package org.telekit.ui.tools.import_file_builder;

import javafx.util.StringConverter;

import java.util.Map;

public class DelimiterStringConverter extends StringConverter<String> {

    private static final Map<String, String> DICT = Map.of(
            ",", "comma",
            ":", "colon",
            "|", "pipe",
            ";", "semicolon",
            "_", "space or TAB"
    );

    @Override
    public String toString(String str) {
        if (str == null || str.isEmpty()) return "";
        return String.format("%s ( %s )", DICT.getOrDefault(str, "unknown"), str);
    }

    @Override
    public String fromString(String str) {
        if (str == null || str.isEmpty()) return "";
        int pos = str.indexOf("(");
        return str.substring(pos + 2, pos + 3);
    }
}
