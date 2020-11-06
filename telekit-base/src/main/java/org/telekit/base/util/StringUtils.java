package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.trim;

public final class StringUtils {

    public static String ensureNotNull(String str) {
        return ensureNotNull(str, "");
    }

    public static String ensureNotNull(String str, String defaultValue) {
        return str != null ? str : defaultValue;
    }

    /**
     * Splits {@code text} to the chunks of equal {@code chunkSize}, e.g.
     * <code>"retina" -> List.of("re", "ti", "na")</code>.
     */
    public static @NotNull List<String> splitEqually(String text, int chunkSize) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        if (chunkSize <= 0 || chunkSize >= text.length()) return List.of(text);

        List<String> result = new ArrayList<>((text.length() + chunkSize - 1) / chunkSize);
        for (int start = 0; start < text.length(); start += chunkSize) {
            result.add(text.substring(start, Math.min(text.length(), start + chunkSize)));
        }
        return result;
    }

    /**
     * Checks whether trimmed strings are equal.
     */
    public static boolean trimEquals(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        return trim(s1).equals(trim(s2));
    }

    public static char[] bytesToChars(byte[] bytes) {
        final CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit());
    }

    public static byte[] charsToBytes(char[] chars) {
        final ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }
}
