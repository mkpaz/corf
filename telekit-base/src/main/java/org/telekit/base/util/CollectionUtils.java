package org.telekit.base.util;

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

    public static <T> List<T> nullToEmpty(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    public static <T> Set<T> nullToEmpty(Set<T> list) {
        return list != null ? list : Collections.emptySet();
    }

    public static <T> T getFirst(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public static <T> T getLast(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public static List<Integer> generate(int startInclusive, int endInclusive) {
        return IntStream.range(startInclusive, endInclusive + 1).boxed().collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> unmodifiableMerge(List<T>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toUnmodifiableList());
    }

    @SafeVarargs
    public static <T> T[] merge(T[]... arrays) {
        int finalLength = 0;
        for (T[] array : arrays) {
            if (array == null || array.length == 0) continue;
            finalLength += array.length;
        }

        T[] dest = null;
        int destPos = 0;

        for (T[] array : arrays) {
            if (array == null || array.length == 0) continue;

            if (dest == null) {
                dest = Arrays.copyOf(array, finalLength);
                destPos = array.length;
            } else {
                System.arraycopy(array, 0, dest, destPos, array.length);
                destPos += array.length;
            }
        }
        return dest;
    }
}
