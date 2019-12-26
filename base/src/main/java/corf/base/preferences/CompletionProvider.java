package corf.base.preferences;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * An abstraction to implement autocompletion (autosuggestion) for arbitrary objects.
 * It's a read-only repository that has unique ID (key) and provides some search
 * methods over its content.
 */
public interface CompletionProvider<T> {

    /** Provider ID and/or name of the parameter this class provides autocompletion for. */
    String key();

    /** Returns the collection of items that matches given string. */
    Collection<T> matches(String s);

    static boolean isValidKey(String key) {
        return StringUtils.isNotBlank(key);
    }
}
