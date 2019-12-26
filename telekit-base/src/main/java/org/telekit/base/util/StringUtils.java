package org.telekit.base.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

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
    public static List<String> splitEqually(String text, int chunkSize) {
        if (text == null || text.isEmpty()) { return Collections.emptyList(); }
        if (chunkSize <= 0 || chunkSize >= text.length()) { return List.of(text); }

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
        if (s1 == null || s2 == null) { return false; }
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

    public static String toBase64(String str) {
        return toBase64(str, StandardCharsets.UTF_8);
    }

    public static String toBase64(String str, Charset charset) {
        if (str == null) { return ""; }
        return new String(Base64.getEncoder().encode(str.getBytes(charset)), charset);
    }

    public static String fromBase64(String str) {
        return toBase64(str, StandardCharsets.UTF_8);
    }

    public static String fromBase64(String str, Charset charset) {
        if (str == null) { return ""; }
        return new String(Base64.getDecoder().decode(str.getBytes(charset)), charset);
    }

    public static <T> String stringify(T object) {
        return stringify(object, "");
    }

    public static <T> String stringify(T object, String defaultStr) {
        return stringify(object, defaultStr, String::valueOf);
    }

    public static <T> String stringify(T object, String defaultStr, Function<T, String> converter) {
        if (object == null) { return defaultStr; }
        return converter.apply(object);
    }
}
