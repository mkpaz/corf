package org.telekit.base.util;

import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class PlaceholderReplacer {

    public static final String PLACEHOLDER_CHARACTERS = "[a-zA-Z0-9_-]+";
    public static final String PLACEHOLDER_PATTERN = "\\%\\([a-zA-Z0-9_-]+\\)";
    public static final Pattern EXPRESSION = Pattern.compile(
            "\\\\(.)" +                 // treat any character after a backslash literally
                    "|" +
                    "(%\\(([^)]+)\\))"  // look for %(keys) to replace
    );

    /**
     * Expands format strings containing <code>%(keys)</code>.
     *
     * <p>Examples:</p>
     *
     * <ul>
     * <li><code>format("Hello, %(name)!", Map.of("name", "world"))</code> → <code>"Hello, world!"</code></li>
     * <li><code>format("Hello, \%(name)!", Map.of("name", "world"))</code> → <code>"Hello, %(name)!"</code></li>
     * <li><code>format("Hello, %(name)!", Map.of("foo", "bar"))</code> → <code>"Hello, %(name)!"</code></li>
     * </ul>
     *
     * @param fmt The format string.  Any character in the format string that
     * follows a backslash is treated literally.  Any
     * <code>%(key)</code> is replaced by its corresponding value
     * in the <code>values</code> map.  If the key does not exist
     * in the <code>values</code> map, then it is left unsubstituted.
     * @param values Key-value pairs to be used in the substitutions.
     *
     * @return The formatted string.
     */
    public static String format(String fmt, Map<String, String> values) {
        if (isBlank(fmt)) return "";
        return EXPRESSION.matcher(fmt)
                .replaceAll(match -> match.group(1) != null ?
                        match.group(1) :
                        values.getOrDefault(match.group(3), match.group(2))
                );
    }

    public static boolean containsPlaceholders(String fmt) {
        return EXPRESSION.matcher(fmt).find();
    }
}
