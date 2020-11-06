package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.domain.LineSeparator;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class CSVUtils {

    public static final String COMMA_OR_SEMICOLON = "[,;]";

    /**
     * Splits CSV text to matrix by using specified separator.
     * Warning: this method does not respect CSV quotes.
     */
    public static @NotNull String[][] textToTable(String text, String valueSeparator) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(valueSeparator);

        if (text.isBlank() || valueSeparator.isBlank()) return new String[][] {};

        String[] rows = text.split(LineSeparator.LINE_SPLIT_PATTERN);
        String[][] table = new String[rows.length][];
        int nonEmptyRows = 0;
        for (String row : rows) {
            if (isBlank(row)) continue;
            table[nonEmptyRows] = row.split(valueSeparator);
            nonEmptyRows++;
        }
        return Arrays.copyOfRange(table, 0, nonEmptyRows);
    }
}
