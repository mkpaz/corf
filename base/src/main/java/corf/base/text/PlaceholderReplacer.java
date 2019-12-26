package corf.base.text;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class PlaceholderReplacer {

    public static final Pattern EXPRESSION = Pattern.compile(
            "\\\\(.)" +                 // treat any character after a backslash literally
                    "|" +
                    "(\\$\\{([^}]+)})"  // look for ${keys} to replace
    );

    /**
     * Expands format strings containing <code>${keys}</code>.
     *
     * <p>Examples:</p>
     *
     * <ul>
     * <li><code>format("Hello, ${name}!", Map.of("name", "world"))</code> → <code>"Hello, world!"</code></li>
     * <li><code>format("Hello, \${name}!", Map.of("name", "world"))</code> → <code>"Hello, ${name}!"</code></li>
     * <li><code>format("Hello, ${name}!", Map.of("foo", "bar"))</code> → <code>"Hello, ${name}!"</code></li>
     * </ul>
     *
     * @param pattern The format string.  Any character in the format string that
     *                follows a backslash is treated literally.  Any
     *                <code>${key}</code> is replaced by its corresponding value
     *                in the <code>values</code> map.  If the key does not exist
     *                in the <code>values</code> map, then it is left unsubstituted.
     * @param values  Key-value pairs to be used in the substitutions.
     * @return The formatted string.
     */
    public static String replace(@Nullable String pattern, @Nullable Map<String, String> values) {
        if (isBlank(pattern)) { return ""; }
        if (values == null || values.isEmpty()) { return pattern; }

        return EXPRESSION.matcher(pattern).replaceAll(match -> {
            var replacement = (match.group(1) != null ?
                    match.group(1) :
                    values.getOrDefault(match.group(3), match.group(2)));

            // quote replacements too, because it will fail miserably if replacement contains either '\' or '$'
            return Matcher.quoteReplacement(replacement);
        });
    }

    public static boolean containsPlaceholders(@Nullable String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        return EXPRESSION.matcher(pattern).find();
    }

    public static String removePlaceholders(@Nullable String pattern) {
        if (isBlank(pattern)) { return ""; }
        return EXPRESSION.matcher(pattern).replaceAll(match -> "");
    }
}
