package org.telekit.base.util;

public final class NumberUtils {

    public static boolean isOdd(int value) {
        return (value & 1) != 0;
    }

    public static boolean isEven(int value) {
        return (value % 2) == 0;
    }

    public static boolean isBetween(int value, int minInclusive, int maxInclusive) {
        return (value >= minInclusive) & (value <= maxInclusive);
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * Ensures that {@code value} falls in range between {@code minInclusive} and {@code maxInclusive}.
     * Returns {@code minInclusive} if <code>value < min</code> and {@code maxInclusive} if <code>value > max</code>,
     * otherwise returns {@code value}.
     */
    public static int ensureRange(int value, int minInclusive, int maxInclusive) {
        return Math.min(Math.max(value, minInclusive), maxInclusive);
    }

    /**
     * Ensures that {@code value} falls in range between {@code min} and {@code max}.
     * Returns {@code defaultValue} if <code>value < min</code> or <code>value > max</code>.
     */
    public static int ensureRange(int value, int minInclusive, int maxInclusive, int defaultValue) {
        return isBetween(value, minInclusive, maxInclusive) ? value : defaultValue;
    }

    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) { return false; }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isHex(String s) {
        if (s == null || s.isEmpty()) { return false; }
        try {
            Integer.parseInt(s, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBinary(String s) {
        if (s == null || s.isEmpty()) { return false; }
        return s.chars().allMatch(c -> c == '0' || c == '1');
    }

    public static int largestBitValue(int bitLength) {
        return (1 << bitLength) - 1; // = 2 ^ n - 1
    }
}
