package org.telekit.base.util;

import org.telekit.base.domain.LineSeparator;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TextUtils {

    public static int countNotBlankLines(String text) {
        int count = 0;
        if (isNotBlank(text)) {
            String textWithoutEmptyLines = text.replaceAll("(?m)^[ \t]*\r?\n", "");
            count = textWithoutEmptyLines.split(LineSeparator.LINE_SPLIT_PATTERN, -1).length;
        }
        return count;
    }
}
