package org.telekit.base.util;

import java.util.List;
import java.util.Random;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class PasswordGenerator {

    private static final Random RANDOM = new Random();

    // @formatter:off
    public static final List<Character> ASCII_LOWER = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    );

    public static final List<Character> ASCII_UPPER = List.of(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    );

    public static final List<Character> ASCII_DIGITS = List.of(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    );

    public static final List<Character> ASCII_SPECIAL_CHARS = List.of(
            '=', '-', '!', '#', '$', '%', '&', '@', '*', '_', '~', '?' // no brackets or another indistinguishable chars
    );

    public static final List<Character> CONSONANTS = List.of(
            'K', 'S', 'T', 'N', 'H', 'M', 'Y', 'R', 'W', 'F', 'G', 'Z', 'D', 'B', 'P'
    );

    public static final List<Character> VOWELS = List.of(
            'a', 'i', 'u', 'e', 'o'
    );
    // @formatter:on

    public static final List<Character> ASCII_LOWER_DIGITS = CollectionUtils.unmodifiableMerge(ASCII_LOWER, ASCII_DIGITS);
    public static final List<Character> ASCII_LOWER_UPPER_DIGITS = CollectionUtils.unmodifiableMerge(ASCII_LOWER, ASCII_UPPER, ASCII_DIGITS);
    public static final List<Character> ASCII_ALL = CollectionUtils.unmodifiableMerge(ASCII_LOWER, ASCII_UPPER, ASCII_DIGITS, ASCII_SPECIAL_CHARS);

    public static String random(int length, List<Character> sequence) {
        if (length <= 0 || sequence == null || sequence.isEmpty()) return "";

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(sequence.get(RANDOM.nextInt(sequence.size())));
        }

        return result.toString();
    }

    public static String katakana(int length) {
        if (length <= 0) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i += 2) {
            result.append(CONSONANTS.get(RANDOM.nextInt(CONSONANTS.size()))) // odd position
                    .append(VOWELS.get(RANDOM.nextInt(VOWELS.size()))); // even position
        }

        return result.substring(0, length);
    }

    public static String onDict(int wordsCount, String separator, List<String> dict) {
        if (wordsCount <= 0 || dict == null || dict.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordsCount; i++) {
            String word = dict.get(RANDOM.nextInt(dict.size()));

            if (NumberUtils.isOdd(RANDOM.nextInt())) {
                word = capitalizeFirstLetter(word);
            } else {
                word = capitalizeLastLetter(word);
            }

            result.append(word);

            if (i + 1 < wordsCount) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    private static String capitalizeFirstLetter(String str) {
        return capitalize(str);
    }

    private static String capitalizeLastLetter(String str) {
        return str.substring(0, str.length() - 1) + str.substring(str.length() - 1).toUpperCase();
    }
}
