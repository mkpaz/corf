package org.telekit.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

public final class StringUtils {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(?<=[a-z])[A-Z]");

    public static String nullToEmpty(String str) {
        return str != null ? str : "";
    }

    public static String blankToEmpty(String str) {
        return isNotBlank(str) ? str : "";
    }

    public static List<String> splitEqually(String text, int size) {
        List<String> result = new ArrayList<>((text.length() + size - 1) / size);
        for (int start = 0; start < text.length(); start += size) {
            result.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return result;
    }

    public static boolean trimEquals(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        return trim(s1).equals(trim(s2));
    }
}
