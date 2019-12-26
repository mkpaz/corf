package corf.base.text;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaceholderReplacerTest {

    private static final String VALID_CHARS = PasswordGenerator.ASCII_ALL.stream()
            .map(c -> c != '}' && c != '{' ? c.toString() : "")
            .collect(Collectors.joining(""));

    @Test
    public void testPlaceholdersReplaced() {
        assertThat(PlaceholderReplacer.replace(
                "Hello, ${name}!",
                Map.of("name", "foo"))
        ).isEqualTo("Hello, foo!");

        assertThat(PlaceholderReplacer.replace(
                "Hello, ${name1} ${name2} ${name3}!",
                Map.of("name1", "foo", "name2", "bar", "name3", "baz"))
        ).isEqualTo("Hello, foo bar baz!");

        assertThat(PlaceholderReplacer.replace(
                "Hello, ${" + VALID_CHARS + "}!",
                Map.of(VALID_CHARS, "foo"))
        ).isEqualTo("Hello, foo!");
    }

    @Test
    public void testUnresolvedPlaceholdersPreserved() {
        assertThat(PlaceholderReplacer.replace("Hello, ${name}!", Map.of("shame", "foo")))
                .isEqualTo("Hello, ${name}!");
    }

    @Test
    public void testEscapedPlaceholdersPreserved() {
        assertThat(PlaceholderReplacer.replace("Hello, \\${name}!", Map.of("name", "foo")))
                .isEqualTo("Hello, ${name}!");
    }

    @Test
    public void testReplacementCanContainMatcherSpecialChars() {
        assertThat(PlaceholderReplacer.replace("Hello, ${name}!", Map.of("name", "\\foo")))
                .isEqualTo("Hello, \\foo!");
        assertThat(PlaceholderReplacer.replace("Hello, ${name}!", Map.of("name", "$foo")))
                .isEqualTo("Hello, $foo!");
    }

    @Test
    public void testContainPlaceholders() {
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, ${" + VALID_CHARS + "}!")).isTrue();
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, ${name}!")).isTrue();
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, \\${name}!")).isTrue();
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, ${}!")).isFalse();
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, {name}!")).isFalse();
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, ${name!")).isFalse();
        assertThat(PlaceholderReplacer.containsPlaceholders("Hello, $name}!")).isFalse();
    }

    @Test
    public void testRemovePlaceholders() {
        assertThat(PlaceholderReplacer.removePlaceholders("Hello, ${name}!")).isEqualTo("Hello, !");
        assertThat(PlaceholderReplacer.removePlaceholders("${" + VALID_CHARS + "}")).isEqualTo("");
        assertThat(PlaceholderReplacer.removePlaceholders("${foo}/${bar}/${baz}!")).isEqualTo("//!");
        assertThat(PlaceholderReplacer.removePlaceholders("Hello, ${}!")).isEqualTo("Hello, ${}!");
    }
}
