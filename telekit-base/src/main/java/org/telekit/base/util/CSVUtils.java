package org.telekit.base.util;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class CSVUtils {

    public static final String CSV_DELIMITER = "[,;]";

    public static int countNotBlankLines(String text) {
        int count = 0;
        if (isNotBlank(text)) {
            String csvNoEmptyLines = text.replaceAll("(?m)^[ \t]*\r?\n", "");
            count = csvNoEmptyLines.split(FileUtils.EOL_SPLIT_PATTERN, -1).length;
        }
        return count;
    }

    public static String[][] splitToTable(String text) {
        String[] lines = text.split(FileUtils.EOL_SPLIT_PATTERN);
        String[][] result = new String[lines.length][];
        int nonEmptyRows = 0;
        for (String line : lines) {
            if (isBlank(line)) continue;
            result[nonEmptyRows] = line.split(CSV_DELIMITER);
            nonEmptyRows++;
        }
        return Arrays.copyOfRange(result, 0, nonEmptyRows);
    }
}
