package org.telekit.base.service.completion;

import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public interface CompletionProvider<T> {

    /**
     * Key (or name) of the parameter this class provides autocompletion for.
     */
    String key();

    Collection<T> startsWith(String str);

    Collection<T> contains(String str);

    Collection<T> matches(String pattern);

    static boolean isValidKey(String key) {
        return isNotBlank(key);
    }
}
