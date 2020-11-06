package org.telekit.base.util;

import java.util.Arrays;

public final class NumberUtils {

    public static boolean isOdd(int value) {
        return (value & 1) != 0;
    }

    public static boolean isEven(int value) {
        return (value % 2) == 0;
    }

    public static boolean inRange(int value, int min, int max) {
        return (value >= min) && (value <= max);
    }

    /**
     * Ensures that {@code value} falls in range between {@code min} and {@code max}.
     * Returns {@code min} if <code>value < min</code> and {@code max} if <code>value > max</code>,
     * otherwise returns {@code value}.
     */
    public static int ensureRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Ensures that {@code value} falls in range between {@code min} and {@code max}.
     * Returns {@code defaultValue} if <code>value < min</code> or <code>value > max</code>.
     */
    public static int ensureRange(int value, int min, int max, int defaultValue) {
        return inRange(value, min, max) ? value : defaultValue;
    }

    public static boolean isOneOf(int value, int... args) {
        return Arrays.stream(args).anyMatch(arg -> arg == value);
    }
}
