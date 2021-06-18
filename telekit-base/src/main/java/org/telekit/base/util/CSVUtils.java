package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.domain.LineSeparator;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.apache.commons.lang3.StringUtils.isBlank;

/*
 * NOTE:
 * This class provides some utils to work with comma-separated strings.
 * It doesn't support CSV as a standard, which means that methods in this class
 * doesn't respect nor CSV quotes nor headers.
 */
public final class CSVUtils {

    public static final String COMMA_OR_SEMICOLON = "[,;]";

    /**
     * Splits CSV text to matrix by using specified separator.
     */
    public static @NotNull String[][] textToTable(String text, String valueSeparator) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(valueSeparator);

        if (text.isBlank() || valueSeparator.isBlank()) { return new String[][] {}; }

        String[] rows = text.split(LineSeparator.LINE_SPLIT_PATTERN);
        String[][] table = new String[rows.length][];
        int nonEmptyRows = 0;
        for (String row : rows) {
            if (isBlank(row)) { continue; }
            table[nonEmptyRows] = row.split(valueSeparator);
            nonEmptyRows++;
        }
        return Arrays.copyOfRange(table, 0, nonEmptyRows);
    }

    public static @NotNull String addColumnsTheRight(String origText, String addedText, String valueSeparator) {
        boolean isOrigBlank = isBlank(origText);
        boolean isAddedBlank = isBlank(addedText);

        if (isOrigBlank && isAddedBlank) { return ""; }
        if (isOrigBlank) { return addedText; }
        if (isAddedBlank) { return origText; }

        String[][] origColumns = textToTable(origText, valueSeparator);
        String[][] addedColumns = textToTable(addedText, valueSeparator);

        StringBuilder sb = new StringBuilder();
        for (int rowIndex = 0; rowIndex < origColumns.length; rowIndex++) {
            String[] curRow = origColumns[rowIndex];

            if (rowIndex < addedColumns.length) {
                String[] newRow = addedColumns[rowIndex];
                sb.append(String.join(",", addAll(curRow, newRow)));
            } else {
                sb.append(String.join(",", curRow));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
