package org.telekit.base.service;

import java.util.*;

@SuppressWarnings("SpellCheckingInspection")
public class RUTransliterator implements Transliterator {

    private static final Map<Character, String> ABC = new HashMap<>();

    static {
        ABC.put('А', "A");
        ABC.put('а', "a");
        ABC.put('Б', "B");
        ABC.put('б', "b");
        ABC.put('В', "V");
        ABC.put('в', "v");
        ABC.put('Г', "G");
        ABC.put('г', "g");
        ABC.put('Д', "D");
        ABC.put('д', "d");
        ABC.put('Е', "E");
        ABC.put('е', "e");
        ABC.put('Ё', "Yo");
        ABC.put('ё', "yo");
        ABC.put('Ж', "Zh");
        ABC.put('ж', "zh");
        ABC.put('З', "Z");
        ABC.put('з', "z");
        ABC.put('И', "I");
        ABC.put('и', "i");
        ABC.put('Й', "Y");
        ABC.put('й', "y");
        ABC.put('К', "K");
        ABC.put('к', "k");
        ABC.put('Л', "L");
        ABC.put('л', "l");
        ABC.put('М', "M");
        ABC.put('м', "m");
        ABC.put('Н', "N");
        ABC.put('н', "n");
        ABC.put('О', "O");
        ABC.put('о', "o");
        ABC.put('П', "P");
        ABC.put('п', "p");
        ABC.put('Р', "R");
        ABC.put('р', "r");
        ABC.put('С', "S");
        ABC.put('с', "s");
        ABC.put('Т', "T");
        ABC.put('т', "t");
        ABC.put('У', "U");
        ABC.put('у', "u");
        ABC.put('Ф', "F");
        ABC.put('ф', "f");
        ABC.put('Х', "Kh");
        ABC.put('х', "kh");
        ABC.put('Ц', "Ts");
        ABC.put('ц', "ts");
        ABC.put('Ч', "Ch");
        ABC.put('ч', "ch");
        ABC.put('Ш', "Sh");
        ABC.put('ш', "sh");
        ABC.put('Щ', "Shch");
        ABC.put('щ', "shch");
        ABC.put('ъ', "y");
        ABC.put('Ъ', "y");
        ABC.put('Ы', "Y");
        ABC.put('ы', "y");
        ABC.put('ь', "y");
        ABC.put('Ь', "Y");
        ABC.put('Э', "E");
        ABC.put('э', "e");
        ABC.put('Ю', "Yu");
        ABC.put('ю', "yu");
        ABC.put('Я', "Ya");
        ABC.put('я', "ya");
    }

    private static final List<Character> CONSONANTS = Arrays.asList(
            'б', 'в', 'г', 'д', 'ж', 'з', 'й', 'к', 'л', 'м', 'н', 'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш', 'щ',
            'Б', 'В', 'Г', 'Д', 'Ж', 'З', 'Й', 'К', 'Л', 'М', 'Н', 'П', 'Р', 'С', 'Т', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ'
    );

    private static final List<Character> VOWELS = Arrays.asList(
            'а', 'у', 'о', 'ы', 'и', 'э', 'я', 'ю', 'ё', 'е',
            'А', 'У', 'О', 'Ы', 'И', 'Э', 'Я', 'Ю', 'Ё', 'Е'
    );

    private static final List<Character> IOTATED_VOWELS = Arrays.asList(
            'е', 'ё', 'ю', 'я', 'Е', 'Ё', 'Ю', 'Я'
    );

    private static final List<Character> NON_IOTATED_VOWELS = Arrays.asList(
            'а', 'и', 'о', 'у', 'ы', 'Э', 'А', 'И', 'О', 'У', 'Ы', 'Э'
    );

    @Override
    public String transliterate(String text) {
        StringBuilder result = new StringBuilder();
        List<Character> word = new ArrayList<>();
        for (int index = 0, length = text.length(); index < length; index++) {
            Character current = text.charAt(index);
            boolean isLetter = isLetter(current);
            boolean endOfText = index == text.length() - 1;

            if (isLetter) {
                word.add(current);
                if (endOfText) transliterateWord(word, result);
            } else {
                if (!word.isEmpty()) {
                    transliterateWord(word, result);
                    word = new ArrayList<>();
                }
                result.append(current);
            }
        }
        return result.toString();
    }

    private void transliterateWord(List<Character> word, StringBuilder textAccumulator) {
        int wordLength = word.size();

        for (int index = 0; index < wordLength; index++) {
            Character current = word.get(index);
            Character prev = index > 0 ? word.get(index - 1) : null;
            Character next = index < wordLength - 1 ? word.get(index + 1) : null;
            boolean startOfWord = index == 0;
            boolean endOfWord = index == wordLength - 1;

            boolean putAsUppercase = Character.isUpperCase(current) &&
                    (endOfWord || (next != null && Character.isUpperCase(next)));

            /*
             * EXCEPTIONS (transliteration depends on position or adjacent chars)
             */

            // Е (е) = Ye (ye), when:
            // - in the beginning of words (Ельцин = Yeltsin)
            // - after vowels (Раздольное = Razdolnoye)
            // - after ь (Юрьев = Yuryev; ь omitted)
            // - after ъ (Подъездной = Podyezdnoy)
            // - ые endings = -ye (Набережные Челны = Naberezhnye Chelny)
            if (equalsIgnoreCase(current, 'е') && !equalsIgnoreCase(prev, 'ы') &&
                    (startOfWord || isVowel(prev) || equalsIgnoreCase(prev, 'ъ') || equalsIgnoreCase(prev, 'ь'))) {
                if (current == 'Е' && putAsUppercase) textAccumulator.append("YE");
                if (current == 'Е' && !putAsUppercase) textAccumulator.append("Ye");
                if (current == 'е') textAccumulator.append("ye");
                continue;
            }

            // Omitted, when:
            // - followed by an iotated vowel (Усолье = Usolye)
            // - at the end of words (Выхухоль = Vykhukhol)
            // - followed by a consonant (Дальнегорск = Dalnegorsk)
            if (equalsIgnoreCase(current, 'ь') && (endOfWord || isIotatedVowel(next) || isConsonant(next))) {
                continue;
            }

            // Omitted, when:
            //  - followed by an iotated vowel (Подъярский = Podyarsky)
            if (equalsIgnoreCase(current, 'ъ') && !endOfWord && isIotatedVowel(next)) {
                continue;
            }

            // -ый ending  = -y  (Красный = Krasny)
            if (equalsIgnoreCase(current, 'й') && endOfWord && equalsIgnoreCase(prev, 'ы')) {
                continue;
            }

            // Following Wikipedia rules:
            // –ий endings	= -y    In names of people and adjectives of Russian origin.
            //                      Синий = Siny; Великий = Veliky
            //              = -iy   Noun or of non-Russian origin
            //                      Рыркайпий = Ryrkaypiy
            //
            // There is now to find out origin of the word without dictionary
            // (and I don't know such dictionary), so let's just stick to the first option.
            if (equalsIgnoreCase(current, 'й') && endOfWord && equalsIgnoreCase(prev, 'и')) {
                textAccumulator.deleteCharAt(textAccumulator.length() - 1);
                if (putAsUppercase) {
                    textAccumulator.append('Y');
                } else {
                    textAccumulator.append('y');
                }

                continue;
            }

            /* DIRECT TRANSLITERATION */
            if (ABC.containsKey(current)) {
                if (putAsUppercase) {
                    textAccumulator.append(ABC.get(current).toUpperCase());
                } else {
                    textAccumulator.append(ABC.get(current));
                }
            } else {
                textAccumulator.append(current);
            }
        }
    }

    private static void appendCaseSensitive(Character character, Character nextCharacter, StringBuilder accumulator) {

    }

    private static boolean isLetter(Character symbol) {
        return symbol != null && ABC.containsKey(symbol);
    }

    private boolean equals(Character ch1, Character ch2) {
        return ch1 != null && ch1.equals(ch2);
    }

    private boolean equalsIgnoreCase(Character ch1, Character ch2) {
        return ch1 != null && ch2 != null && Objects.equals(Character.toLowerCase(ch1), Character.toLowerCase(ch2));
    }

    private static boolean isConsonant(Character letter) {
        return letter != null && CONSONANTS.contains(letter);
    }

    private static boolean isVowel(Character letter) {
        return letter != null && VOWELS.contains(letter);
    }

    private static boolean isIotatedVowel(Character letter) {
        return letter != null && IOTATED_VOWELS.contains(letter);
    }

    private static boolean isNonIotatedVowel(Character letter) {
        return letter != null && NON_IOTATED_VOWELS.contains(letter);
    }
}
