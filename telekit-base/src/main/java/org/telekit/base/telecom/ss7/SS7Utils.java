package org.telekit.base.telecom.ss7;

import org.telekit.base.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.util.NumberUtils.inRange;
import static org.telekit.base.util.NumberUtils.isOneOf;

public final class SS7Utils {

    public static final String SPC_STRUCT_SEPARATOR = "-";

    public static final List<SPCFormat> FORMATS_14_BIT = Arrays.asList(
            SPCFormat.DEC, SPCFormat.BIN, SPCFormat.HEX, SPCFormat.STRUCT_383, SPCFormat.STRUCT_86
    );

    public static final List<SPCFormat> FORMATS_24_BIT = Arrays.asList(
            SPCFormat.DEC, SPCFormat.BIN, SPCFormat.HEX, SPCFormat.STRUCT_888
    );

    public static final int MIN_CIC = 1;
    public static final int MAX_CIC = 1 << 12; // 2 ^ 12 = 4096

    public enum SPCFormat {
        DEC("DECIMAL"),
        HEX("HEX"),
        BIN("BINARY"),
        STRUCT_383("3-8-3"),
        STRUCT_86("8-6"),
        STRUCT_888("8-8-8");

        public final String description;

        SPCFormat(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static int parsePointCode(String str, SPCFormat format, int length) {
        if (isBlank(str) || format == null) return -1;

        boolean valid = false;
        int spc = -1;

        switch (format) {
            case DEC:
                if (isInteger(str)) {
                    spc = parseInt(str);
                    valid = inRange(spc, 1, maxNumberInBits(length));
                }
                break;
            case HEX:
                if (isHex(str)) {
                    spc = parseInt(str, 16);
                    valid = inRange(spc, 1, maxNumberInBits(length));
                }
                break;
            case BIN:
                if (isBinary(str)) {
                    spc = parseInt(str, 2);
                    valid = inRange(spc, 1, maxNumberInBits(length));
                }
                break;
            case STRUCT_383:
                if (isStructInteger(str, 3)) {
                    String[] parts = str.split(SPC_STRUCT_SEPARATOR, -1);
                    valid = inRange(parseInt(parts[0]), 0, maxNumberInBits(3)) &&
                            inRange(parseInt(parts[1]), 0, maxNumberInBits(8)) &&
                            inRange(parseInt(parts[2]), 1, maxNumberInBits(3));
                    spc = parseInt(toBinaryString(parts[0], 3) +
                                    toBinaryString(parts[1], 8) +
                                    toBinaryString(parts[2], 3)
                            , 2);
                }
                break;
            case STRUCT_86:
                if (isStructInteger(str, 2)) {
                    String[] parts = str.split(SPC_STRUCT_SEPARATOR, -1);
                    valid = inRange(parseInt(parts[0]), 0, maxNumberInBits(8)) &&
                            inRange(parseInt(parts[1]), 1, maxNumberInBits(6));
                    spc = parseInt(toBinaryString(parts[0], 8) +
                                    toBinaryString(parts[1], 6)
                            , 2);
                }
                break;
            case STRUCT_888:
                if (isStructInteger(str, 3)) {
                    String[] parts = str.split(SPC_STRUCT_SEPARATOR, -1);
                    valid = inRange(parseInt(parts[0]), 0, maxNumberInBits(8)) &&
                            inRange(parseInt(parts[1]), 0, maxNumberInBits(8)) &&
                            inRange(parseInt(parts[2]), 1, maxNumberInBits(8));
                    spc = parseInt(toBinaryString(parts[0], 8) +
                                    toBinaryString(parts[1], 8) +
                                    toBinaryString(parts[2], 8)
                            , 2);
                }
                break;
        }

        return valid ? spc : -1;
    }

    public static String formatPointCode(int spc, int length, SPCFormat format) {
        if (spc < 0 || !isOneOf(length, 14, 24) || format == null) return null;
        return switch (format) {
            case DEC -> String.valueOf(spc);
            case HEX -> Integer.toHexString(spc).toUpperCase();
            case BIN -> leftPad(Integer.toBinaryString(spc), length, "0");
            case STRUCT_383 -> toStructInteger(spc, length, new int[] {3, 8, 3});
            case STRUCT_86 -> toStructInteger(spc, length, new int[] {8, 6});
            case STRUCT_888 -> toStructInteger(spc, length, new int[] {8, 8, 8});
        };
    }

    public static List<Integer> getCICRange(int e1Num) {
        if (e1Num < 0) return Collections.emptyList();
        return CollectionUtils.generate(e1Num * 32 + 1, e1Num * 32 + 31);
    }

    public static Integer findE1ByCIC(int cic) {
        if (cic < MIN_CIC || cic > MAX_CIC) return -1;
        return cic / 32;
    }

    public static Integer findTimeslotByCIC(int cic) {
        if (cic < MIN_CIC || cic > MAX_CIC) return -1;
        return cic % 32;
    }

    private static int maxNumberInBits(int bitLength) {
        return (1 << bitLength) - 1; // = 2 ^ n - 1
    }

    private static boolean isStructInteger(String str, int length) {
        if (isEmpty(str)) return false;
        String[] parts = str.split(SPC_STRUCT_SEPARATOR, -1);
        return parts.length == length && Arrays.stream(parts).allMatch(s -> isNotEmpty(s) && isInteger(s));
    }

    private static String toBinaryString(String intString, int length) {
        return leftPad(
                Integer.toBinaryString(parseInt(intString)), length, "0"
        );
    }

    private static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBinary(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.chars().allMatch(c -> c == '0' || c == '1');
    }

    private static boolean isHex(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String toStructInteger(int spc, int length, int[] proportion) {
        if (length != Arrays.stream(proportion).sum()) {
            throw new IllegalArgumentException("Proportion should fit SPC length");
        }

        int start = 0;
        List<String> parts = new ArrayList<>(proportion.length);
        String paddedSpc = leftPad(Integer.toBinaryString(spc), length, "0");

        for (int end : proportion) {
            parts.add(String.valueOf(
                    parseInt(paddedSpc.substring(start, start + end), 2)
            ));
            start += end;
        }

        return String.join(SPC_STRUCT_SEPARATOR, parts);
    }
}
