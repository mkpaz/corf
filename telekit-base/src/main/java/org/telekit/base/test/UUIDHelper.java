package org.telekit.base.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UUIDHelper {

    private UUIDHelper() {}

    public static UUID fromInt(int number) {
        long leastSignificantBits = Long.parseLong(Integer.toString(Math.max(number, 0)), 16) & 281474976710655L;
        return new UUID(0, leastSignificantBits);
    }

    public static List<UUID> fromArray(int... positiveNumbers) {
        return Arrays.stream(positiveNumbers)
                .mapToObj(UUIDHelper::fromInt)
                .collect(Collectors.toList());
    }

    public static List<UUID> fromCollection(Collection<Integer> integers) {
        return integers.stream()
                .map(UUIDHelper::fromInt)
                .collect(Collectors.toList());
    }

    public static List<UUID> fromRange(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .mapToObj(UUIDHelper::fromInt)
                .collect(Collectors.toList());
    }
}