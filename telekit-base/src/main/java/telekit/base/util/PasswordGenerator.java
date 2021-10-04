package telekit.base.util;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static telekit.base.util.CollectionUtils.unmodifiableMerge;

public final class PasswordGenerator {

    public static final int DEFAULT_PASSWORD_LENGTH = 12;

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

    // no brackets or another indistinguishable chars
    public static final List<Character> ASCII_SPECIAL_CHARS = List.of(
            '=', '-', '!', '#', '$', '%', '&', '@', '*', '_', '~', '?'
    );

    public static final List<Character> CONSONANTS = List.of(
            'k', 's', 't', 'n', 'h', 'm', 'y', 'r', 'w', 'f', 'g', 'z', 'd', 'b', 'p'
    );

    public static final List<Character> VOWELS = List.of(
            'a', 'i', 'u', 'e', 'o'
    );
    // @formatter:on

    public static final List<Character> ASCII_LOWER_DIGITS = unmodifiableMerge(ASCII_LOWER, ASCII_DIGITS);
    public static final List<Character> ASCII_LOWER_UPPER_DIGITS = unmodifiableMerge(ASCII_LOWER, ASCII_UPPER, ASCII_DIGITS);
    public static final List<Character> ASCII_ALL = unmodifiableMerge(ASCII_LOWER, ASCII_UPPER, ASCII_DIGITS, ASCII_SPECIAL_CHARS);

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

    public static String katakana(int length, boolean pascalCase) {
        if (length <= 0) { length = DEFAULT_PASSWORD_LENGTH; }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i += 2) {
            char consonant = pickRandomFrom(CONSONANTS);
            if (pascalCase) { consonant = Character.toUpperCase(consonant); }
            result.append(consonant);              // odd pos
            result.append(pickRandomFrom(VOWELS)); // even pos
        }

        return result.substring(0, length);
    }

    public static String katakana(int length) {
        return katakana(length, true);
    }

    public static String katakana() {
        return katakana(DEFAULT_PASSWORD_LENGTH);
    }

    public static String xkcd(int wordsCount, String separator, List<String> dict) {
        if (wordsCount <= 0 || dict == null || dict.isEmpty()) { return ""; }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordsCount; i++) {
            result.append(pickRandomFrom(dict));

            if (i + 1 < wordsCount) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    public static <T> T pickRandomFrom(List<T> list) {
        return Objects.requireNonNull(list).get(RANDOM.nextInt(list.size()));
    }
}
