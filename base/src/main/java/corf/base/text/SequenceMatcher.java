package corf.base.text;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The utility class to obtain first free string in a sequence.
 * <p>
 * Consider the following example. We have a list of the filenames named like this:
 * {@code file1.txt, file2.txt, file5.txt} and you want to create a new file with the
 * file name that follows the same pattern and fills the gaps.
 * Namely, {@code file3.txt, file4.txt, file6.txt} and so on. That's the sole purpose of this class.
 * <p>
 * Note that <b>start index can be omitted</b>. The matcher treats 'foo' and 'foo1' equally, and
 * you can choose whether to omit start index when obtaining a new string or not, respectively.
 * That's deliberate, because sometimes we need {@code file.txt, file2.txt, file3.txt} and not
 * {@code file1.txt, file2.txt, file3.txt}.
 */
public final class SequenceMatcher<T> {

    private final String prefix;
    private final @Nullable String separator;
    private final Pattern pattern;
    private int start = 1;
    private @Nullable Function<T, String> extractor;

    /**
     * Creates a new instance.
     * <p>
     * Warning: Do not make the separator a part of the prefix, because this use case requires
     * a special treatment. When separator is a part of prefix and start index is omitted  you'll
     * get 'file_' instead of 'file'.
     *
     * @param prefix    common sequence prefix
     * @param separator (optional) separator between the prefix and index parts of the string
     */
    private SequenceMatcher(String prefix, @Nullable String separator) {
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        this.separator = separator;
        this.pattern = separator != null
                ? Pattern.compile(Pattern.quote(prefix) + "(" + Pattern.quote(separator) + "(\\d+))?")
                : Pattern.compile(Pattern.quote(prefix) + "(\\d+)?");
    }

    /** Sets the initial index. Default is '1' because we are humans. */
    public SequenceMatcher<T> setStartIndex(int x) {
        this.start = x;
        return this;
    }

    /**
     * Sets the function to extract the string from the list of existing items.
     * If not specified, extractor will simply use {@code String.valueOf()} to convert each
     * list element to string and match it against the {@link #prefix}. Use this method to
     * match the {@link #prefix} against the specific object property or transform list string
     * before matching.
     */
    public SequenceMatcher<T> setExtractor(@Nullable Function<T, String> extractor) {
        this.extractor = extractor;
        return this;
    }

    /** See {@link #get(List, boolean)}. */
    public String get(@Nullable List<T> list) {
        return get(list, false);
    }

    /**
     * Returns the first free string in a sequence taking into account given list
     * of existing items.
     *
     * @param list      the list of existing items
     * @param omitStart whether to omit start index or not
     *                  (resulting string will be {@code file} instead of {@code file1})
     */
    public String get(@Nullable List<T> list, boolean omitStart) {
        int index = findFreeIndex(getExistingIndices(list));
        return prefix + (omitStart && index == start ? "" : Objects.toString(separator, "") + index);
    }

    private List<Integer> getExistingIndices(@Nullable List<T> list) {
        var indices = new ArrayList<Integer>();
        if (list == null) { return indices; }

        // filter for duplicates
        var filter = new HashSet<Integer>();

        for (T item : list) {
            String str = extractor != null ? extractor.apply(item) : String.valueOf(item);
            Matcher m = pattern.matcher(str);

            if (!m.matches()) { continue; }

            int group = separator != null ? 2 : 1;
            var index = m.group(group) != null
                    ? Integer.parseInt(m.group(group)) // name = prefix + suffix
                    : start; // name = prefix

            if (index >= start && !filter.contains(index)) {
                indices.add(index);
                filter.add(index);
            }
        }

        return indices;
    }

    private int findFreeIndex(List<Integer> indices) {
        if (indices.isEmpty()) { return start; }

        Collections.sort(indices);

        var firstIndex = indices.get(0);
        var lastIndex = indices.get(indices.size() - 1);

        // indices list has a gap at the beginning
        if (firstIndex != start) { return start; }

        // indices list has a gap in the middle
        if (lastIndex != indices.size() - 1) {
            // starting from 1, because corner case already handled earlier
            for (int i = 1; i < indices.size(); i++) {
                var prev = indices.get(i - 1);
                var current = indices.get(i);

                // found a gap
                if (current != prev + 1) { return prev + 1; }
            }
        }

        // indices list has no gaps
        return lastIndex + 1;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static <T> SequenceMatcher<T> create(String prefix) {
        return new SequenceMatcher<>(prefix, null);
    }

    public static <T> SequenceMatcher<T> create(String prefix, @Nullable String separator) {
        return new SequenceMatcher<>(prefix, separator);
    }
}
