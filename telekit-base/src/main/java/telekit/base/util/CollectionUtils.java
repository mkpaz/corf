package telekit.base.util;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CollectionUtils {

    public static @Nullable <T> T getFirstElement(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public static @Nullable <T> T getLastElement(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public static List<Integer> generate(int startInclusive, int endInclusive) {
        return IntStream.range(startInclusive, endInclusive + 1)
                .boxed()
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        return Arrays.stream(Objects.requireNonNull(lists))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> unmodifiableMerge(List<T>... lists) {
        return Arrays.stream(Objects.requireNonNull(lists))
                .flatMap(Collection::stream)
                .toList();
    }
}
