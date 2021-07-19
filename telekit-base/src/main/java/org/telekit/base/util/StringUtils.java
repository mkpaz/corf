package org.telekit.base.util;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class StringUtils {

    /**
     * Splits string to the chunks of equal {@code chunkSize}, e.g.
     * <code>"retina" -> List.of("re", "ti", "na")</code>.
     */
    public static List<String> splitEqually(String s, int chunkSize) {
        if (s == null || s.isEmpty()) { return Collections.emptyList(); }
        if (chunkSize <= 0 || chunkSize >= s.length()) { return List.of(s); }

        List<String> result = new ArrayList<>((s.length() + chunkSize - 1) / chunkSize);
        for (int start = 0; start < s.length(); start += chunkSize) {
            result.add(s.substring(start, Math.min(s.length(), start + chunkSize)));
        }
        return result;
    }

    public static char[] bytesToChars(byte[] bytes) {
        final CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit());
    }

    public static byte[] charsToBytes(char[] chars) {
        final ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }

    public static @Nullable String toBase64(String s) {
        return toBase64(s, StandardCharsets.UTF_8);
    }

    public static @Nullable String toBase64(String s, Charset charset) {
        if (s == null) { return null; }
        return new String(Base64.getEncoder().encode(s.getBytes(charset)), charset);
    }

    public static @Nullable String fromBase64(String s) {
        return toBase64(s, StandardCharsets.UTF_8);
    }

    public static @Nullable String fromBase64(String s, Charset charset) {
        if (s == null) { return null; }
        return new String(Base64.getDecoder().decode(s.getBytes(charset)), charset);
    }
}
