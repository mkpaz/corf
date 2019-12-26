package corf.base.common;

public final class NumberUtils {

    private NumberUtils() { }

    public static boolean isOdd(int val) {
        return (val & 1) != 0;
    }

    public static boolean isEven(int val) {
        return !isOdd(val);
    }

    /**
     * Verifies that {@code val >= min} and {@code val <= max}.
     *
     * @param val value to be checked
     * @param min min inclusive
     * @param max max inclusive
     */
    public static boolean isBetween(int val, int min, int max) {
        return !(val < min || val > max);
    }

    /** Rounds value to the given precision. */
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * Ensures that {@code val} falls in range between {@code min} and {@code max}.
     * Returns {@code min} if {@code val < min} and {@code max} if {@code val > max},
     * otherwise returns {@code val} itself.
     *
     * @param val value to be checked
     * @param min min inclusive
     * @param max max inclusive
     */
    public static int ensureRange(int val, int min, int max) {
        return Math.min(Math.max(val, min), max);
    }

    /**
     * Ensures that {@code value} falls in range between {@code min} and {@code max}.
     * Returns the {@code defaultVal} if {@code value < min} or {@code value > max}.
     *
     * @param val value to be checked
     * @param min min inclusive
     * @param max max inclusive
     */
    public static int ensureRange(int val, int min, int max, int defaultVal) {
        return isBetween(val, min, max) ? val : defaultVal;
    }
}
