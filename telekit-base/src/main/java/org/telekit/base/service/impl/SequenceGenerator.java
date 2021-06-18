package org.telekit.base.service.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Generates list of maps each entry of which contains a value of corresponding {@link Item}.
 * <p>Given items:</p>
 * <pre>
 *   Item= { id="A", start=10, step=1, count=2 }
 *   Item= { id="B", start=0,  step=1, count=2 }
 * </pre>
 * <p>Result:</p>
 * <pre>
 * [
 *   {
 *      { key="A", value=10 },
 *      { key="B", value=0  }
 *   },
 *   {
 *      { key="A", value=10 },
 *      { key="B", value=1  }
 *   },
 *   {
 *      { key="A", value=11 },
 *      { key="B", value=0  }
 *   },
 *   {
 *      { key="A", value=11 },
 *      { key="B", value=1  }
 *   }
 * ]
 * </pre>
 */
public class SequenceGenerator<ID, V> {

    private final List<Item<ID>> items;
    // converter function can use item ID and its current number value to obtain any
    // required output value type (including strings and objects)
    private final BiFunction<ID, Double, V> converter;

    private final Map<ID, V> valuesAccumulator = new HashMap<>();

    public SequenceGenerator(List<Item<ID>> items, BiFunction<ID, Double, V> converter) {
        this.items = Objects.requireNonNull(items);
        this.converter = Objects.requireNonNull(converter);
    }

    public @NotNull List<Map<ID, V>> generate() {
        Objects.requireNonNull(converter);
        if (items.isEmpty()) { return Collections.emptyList(); }

        List<Map<ID, V>> sequence = new ArrayList<>();
        iterate(Objects.requireNonNull(nextItem(null)), sequence);
        return sequence;
    }

    public static <ID> long expectedSize(List<Item<ID>> items) {
        return items.stream()
                .map(item -> item.count)
                .reduce(1, (a, b) -> a * b);
    }

    private void iterate(Item<ID> currentItem, List<Map<ID, V>> sequenceAccumulator) {
        ID id = currentItem.id;
        double value = currentItem.start;
        Item<ID> nextItem = nextItem(currentItem);

        for (int idx = 0; idx < currentItem.count; idx++) {
            // there's no need to create a new map to accumulate values
            // by the end of the recursion cycle all old values will be replaced to the new ones
            valuesAccumulator.put(currentItem.id, converter.apply(id, value));

            // if next item is not null go deeper to fill values accu,ulator
            if (nextItem != null) {
                iterate(nextItem, sequenceAccumulator);
            } else {
                // otherwise, dump values accumulator into resulting sequence
                sequenceAccumulator.add(new HashMap<>(valuesAccumulator));
            }

            value = value + currentItem.step;
        }
    }

    private @Nullable Item<ID> nextItem(Item<ID> currentItem) {
        // next item is current item + 1 or null if there's no next item
        if (currentItem == null) { return items.get(0); }
        int currentIndex = items.indexOf(currentItem);
        return currentIndex < items.size() - 1 ? items.get(currentIndex + 1) : null;
    }

    public static class Item<ID> {

        public final @NotNull ID id;
        public final double start;
        public final int step;
        public final int count;

        public Item(ID id, double start, int step, int count) {
            this.id = Objects.requireNonNull(id);
            this.start = start;
            this.step = step;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Item<?> item = (Item<?>) o;
            return id.equals(item.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + id +
                    ", start=" + start +
                    ", step=" + step +
                    ", count=" + count +
                    '}';
        }
    }
}
