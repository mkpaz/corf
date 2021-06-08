package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CollectionUtils {

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static @NotNull <T> List<T> ensureNotNull(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    public static @NotNull <T> Set<T> ensureNotNull(Set<T> set) {
        return set != null ? set : Collections.emptySet();
    }

    public static @Nullable <T> T getFirst(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public static @Nullable <T> T getLast(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public static @NotNull List<Integer> generate(int startInclusive, int endInclusive) {
        return IntStream.range(startInclusive, endInclusive + 1).boxed().collect(Collectors.toList());
    }

    @SafeVarargs
    public static @NotNull <T> List<T> merge(List<T>... lists) {
        return Arrays.stream(Objects.requireNonNull(lists))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static @NotNull <T> List<T> unmodifiableMerge(List<T>... lists) {
        return Arrays.stream(Objects.requireNonNull(lists)).flatMap(Collection::stream).toList();
    }
}
