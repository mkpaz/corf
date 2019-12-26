package corf.base.text;

import java.util.*;

@SuppressWarnings("unused")
public final class PasswordGenerator {

    public static final int DEFAULT_PASSWORD_LENGTH = 16;

    private static final Random RANDOM = new Random();

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

    public static final List<Character> HEX = List.of(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    );

    public static final List<Character> LOGOGRAM_CHARS = List.of(
            '#', '$', '%', '&', '@', '^', '`', '~'
    );

    public static final List<Character> PUNCTUATION_CHARS = List.of(
            '.', ',', ':', ';'
    );

    public static final List<Character> QUOTE_CHARS = List.of(
            '"', '\''
    );

    public static final List<Character> SLASH_DASH_CHARS = List.of(
            '\\', '/', '|', '-', '_'
    );

    public static final List<Character> MATH_CHARS = List.of(
            '<', '>', '*', '+', '!', '?', '='
    );

    public static final List<Character> BRACE_CHARS = List.of(
            '{', '[', '(', ')', ']', '}'
    );

    public static final List<Character> CONSONANTS = List.of(
            'k', 's', 't', 'n', 'h', 'm', 'y', 'r', 'w', 'f', 'g', 'z', 'd', 'b', 'p'
    );

    public static final List<Character> VOWELS = List.of(
            'a', 'i', 'u', 'e', 'o'
    );

    public static final List<Character> SIMILAR_CHARS = List.of(
            'i', 'I', 'L', 'l', '1', '0', 'o', 'O'
    );

    public static final List<Character> ASCII_LOWER_DIGITS = merge(
            ASCII_LOWER, ASCII_DIGITS
    );

    public static final List<Character> ASCII_LOWER_UPPER_DIGITS = merge(
            ASCII_LOWER, ASCII_UPPER, ASCII_DIGITS
    );

    public static final List<Character> ASCII_SPECIAL_CHARS = merge(
            LOGOGRAM_CHARS, PUNCTUATION_CHARS, QUOTE_CHARS, SLASH_DASH_CHARS, MATH_CHARS, BRACE_CHARS
    );

    public static final List<Character> ASCII_ALL = merge(
            ASCII_LOWER, ASCII_UPPER, ASCII_DIGITS, ASCII_SPECIAL_CHARS
    );

    public static String random(int length, List<Character> sequence) {
        if (length <= 0) { length = DEFAULT_PASSWORD_LENGTH; }
        if (sequence == null || sequence.isEmpty()) { sequence = ASCII_LOWER_UPPER_DIGITS; }

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(pickRandomFrom(sequence));
        }

        return result.toString();
    }

    public static String random() {
        return random(DEFAULT_PASSWORD_LENGTH, ASCII_LOWER_UPPER_DIGITS);
    }

    public static String katakana(int length, boolean capitalize) {
        if (length <= 0) {
            length = DEFAULT_PASSWORD_LENGTH;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i += 2) {
            char consonant = pickRandomFrom(CONSONANTS);
            if (capitalize) {
                consonant = Character.toUpperCase(consonant);
            }
            result.append(consonant);              // odd pos
            result.append(pickRandomFrom(VOWELS)); // even pos
        }

        return result.substring(0, length);
    }

    public static String passphrase(int wordCount, String separator, List<String> dict) {
        return passphrase(wordCount, separator, dict, false);
    }

    public static String passphrase(int wordCount, String separator, List<String> dict, boolean capitalize) {
        if (wordCount <= 0 || dict == null || dict.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            var word = pickRandomFrom(dict);

            if (capitalize) {
                word = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            }

            result.append(word);

            if (i + 1 < wordCount) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    public static String hex(int length) {
        return random(length, HEX);
    }

    private static <T> T pickRandomFrom(List<T> list) {
        return Objects.requireNonNull(list).get(RANDOM.nextInt(list.size()));
    }

    @SafeVarargs
    private static List<Character> merge(List<Character>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).toList();
    }
}
