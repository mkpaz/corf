package corf.base.text;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("unused")
public final class StringUtils {

    /**
     * Splits string to the chunks of equal {@code chunkSize}, e.g.
     * <code>"retina" -> List.of("re", "ti", "na")</code>.
     */
    public static List<String> splitEqually(@Nullable String s, int chunkSize) {
        if (s == null || s.isEmpty()) { return Collections.emptyList(); }
        if (chunkSize <= 0 || chunkSize >= s.length()) { return List.of(s); }

        List<String> result = new ArrayList<>((s.length() + chunkSize - 1) / chunkSize);
        for (int start = 0; start < s.length(); start += chunkSize) {
            result.add(s.substring(start, Math.min(s.length(), start + chunkSize)));
        }
        return Collections.unmodifiableList(result);
    }

    /** Converts bytes to char array. */
    @SuppressWarnings("ByteBufferBackingArray")
    public static char[] bytesToChars(byte[] bytes) {
        if (bytes == null) { return new char[] { }; }

        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit());
    }

    /** Converts chars to byte array. */
    @SuppressWarnings("ByteBufferBackingArray")
    public static byte[] charsToBytes(char[] chars) {
        if (chars == null) { return new byte[] { }; }

        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }

    /** See {@link #toBase64(String, Charset)}. */
    public static @Nullable String toBase64(@Nullable String s) {
        return toBase64(s, StandardCharsets.UTF_8);
    }

    /** Encodes given string to base64 string. */
    public static @Nullable String toBase64(@Nullable String s, @Nullable Charset charset) {
        if (s == null) { return null; }
        var c = Objects.requireNonNullElse(charset, StandardCharsets.UTF_8);

        return new String(Base64.getEncoder().encode(s.getBytes(c)), c);
    }

    /** See {@link #fromBase64(String, Charset)}. */
    public static @Nullable String fromBase64(@Nullable String s) {
        return toBase64(s, StandardCharsets.UTF_8);
    }

    /** Decodes base64 string. */
    public static @Nullable String fromBase64(@Nullable String s, @Nullable Charset charset) {
        if (s == null) { return null; }
        var c = Objects.requireNonNullElse(charset, StandardCharsets.UTF_8);

        return new String(Base64.getDecoder().decode(s.getBytes(c)), c);
    }
}
