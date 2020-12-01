package org.telekit.base.telecom.ss7;

import org.telekit.base.domain.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.util.NumberUtils.*;

public class SignallingPointCode {

    public static final String STRUCT_SEPARATOR = "-";
    public static final int LEN_ITU = 14;
    public static final int LEN_ANSI = 24;

    public enum Format {
        DEC("DECIMAL"),
        HEX("HEX"),
        BIN("BINARY"),
        STRUCT_383("3-8-3"),
        STRUCT_86("8-6"),
        STRUCT_888("8-8-8");

        public final String description;

        Format(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // european (ITU) formats
    public static final List<Format> FORMATS_14_BIT = Arrays.asList(
            Format.DEC, Format.BIN, Format.HEX, Format.STRUCT_383, Format.STRUCT_86
    );

    // ANSI format
    public static final List<Format> FORMATS_24_BIT = Arrays.asList(
            Format.DEC, Format.BIN, Format.HEX, Format.STRUCT_888
    );

    // Store point code value as integer which can be easily converted to any other format when needed.
    // Max SPC value is 2 ^ 24 = 16777216, so integer type is enough.
    private final int value;
    private final int length;

    private SignallingPointCode(int value, int length) {
        this.value = value;
        this.length = length;
    }

    public int getValue() {
        return value;
    }

    public int getLength() {
        return length;
    }

    public String toString(Format fmt) {
        return switch (fmt) {
            case DEC -> String.valueOf(value);
            case HEX -> Integer.toHexString(value).toUpperCase();
            case BIN -> toBinaryString(value, length);
            case STRUCT_383 -> toStructInteger(value, length, new int[] {3, 8, 3});
            case STRUCT_86 -> toStructInteger(value, length, new int[] {8, 6});
            case STRUCT_888 -> toStructInteger(value, length, new int[] {8, 8, 8});
        };
    }

    public static SignallingPointCode parse(String str, Format fmt, int length) throws InvalidInputException {
        if (isBlank(str) || fmt == null || !isOneOf(length, LEN_ITU, LEN_ANSI)) {
            throw new InvalidInputException("Unable to parse point code: invalid input data.");
        }

        int value = -1;
        boolean valid = false;

        switch (fmt) {
            case DEC:
                if (isInteger(str)) {
                    value = parseInt(str);
                    valid = between(value, 1, largestBitValue(length));
                }
                break;
            case HEX:
                if (isHex(str)) {
                    value = parseInt(str, 16);
                    valid = between(value, 1, largestBitValue(length));
                }
                break;
            case BIN:
                if (isBinary(str)) {
                    value = parseInt(str, 2);
                    valid = between(value, 1, largestBitValue(length));
                }
                break;
            case STRUCT_383:
                if (isStructInteger(str, 3)) {
                    String[] parts = str.split(STRUCT_SEPARATOR, -1);
                    valid = between(parseInt(parts[0]), 0, largestBitValue(3)) &&
                            between(parseInt(parts[1]), 0, largestBitValue(8)) &&
                            between(parseInt(parts[2]), 1, largestBitValue(3));
                    value = parseInt(toBinaryString(parts[0], 3) +
                                             toBinaryString(parts[1], 8) +
                                             toBinaryString(parts[2], 3)
                            , 2);
                }
                break;
            case STRUCT_86:
                if (isStructInteger(str, 2)) {
                    String[] parts = str.split(STRUCT_SEPARATOR, -1);
                    valid = between(parseInt(parts[0]), 0, largestBitValue(8)) &&
                            between(parseInt(parts[1]), 1, largestBitValue(6));
                    value = parseInt(toBinaryString(parts[0], 8) +
                                             toBinaryString(parts[1], 6)
                            , 2);
                }
                break;
            case STRUCT_888:
                if (isStructInteger(str, 3)) {
                    String[] parts = str.split(STRUCT_SEPARATOR, -1);
                    valid = between(parseInt(parts[0]), 0, largestBitValue(8)) &&
                            between(parseInt(parts[1]), 0, largestBitValue(8)) &&
                            between(parseInt(parts[2]), 1, largestBitValue(8));
                    value = parseInt(toBinaryString(parts[0], 8) +
                                             toBinaryString(parts[1], 8) +
                                             toBinaryString(parts[2], 8)
                            , 2);
                }
                break;
        }

        if (!valid || value < 0) {
            throw new InvalidInputException("Unable to parse point code: invalid input data.");
        }

        return new SignallingPointCode(value, length);
    }

    private static String toBinaryString(String str, int length) {
        return toBinaryString(parseInt(str), length);
    }

    private static String toBinaryString(int value, int length) {
        return leftPad(Integer.toBinaryString(value), length, "0");
    }

    private static boolean isStructInteger(String str, int length) {
        if (isEmpty(str)) return false;
        String[] parts = str.split(STRUCT_SEPARATOR, -1);
        return parts.length == length && Arrays.stream(parts).allMatch(s -> isNotEmpty(s) && isInteger(s));
    }

    private static String toStructInteger(int value, int length, int[] proportion) {
        if (length != Arrays.stream(proportion).sum()) {
            throw new IllegalArgumentException("Proportion should fit SPC length");
        }

        int start = 0;
        List<String> parts = new ArrayList<>(proportion.length);
        String paddedValue = toBinaryString(value, length);

        for (int end : proportion) {
            int part = parseInt(paddedValue.substring(start, start + end), 2);
            parts.add(String.valueOf(part));
            start += end;
        }

        return String.join(STRUCT_SEPARATOR, parts);
    }
}
