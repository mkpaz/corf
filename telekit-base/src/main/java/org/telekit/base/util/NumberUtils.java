package org.telekit.base.util;

import java.util.Arrays;

public final class NumberUtils {

    public static boolean isOdd(int value) {
        return (value & 1) != 0;
    }

    public static boolean isEven(int value) {
        return value % 2 == 0;
    }

    public static boolean inRange(int value, int min, int max) {
        return (value >= min) && (value <= max);
    }

    public static int ensureRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int ensureRange(int value, int min, int max, int defaultValue) {
        return inRange(value, min, max) ? value : defaultValue;
    }

    public static boolean isOneOf(int value, int... args) {
        return Arrays.stream(args).anyMatch(arg -> arg == value);
    }
}
