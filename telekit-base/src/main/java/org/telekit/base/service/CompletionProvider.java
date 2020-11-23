package org.telekit.base.service;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface CompletionProvider<T> {

    /**
     * Key (or name) of the parameter this class provides autocompletion for.
     */
    @NotNull String key();

    @NotNull Collection<T> startsWith(String str);

    @NotNull Collection<T> contains(String str);

    @NotNull Collection<T> matches(String pattern);
}
