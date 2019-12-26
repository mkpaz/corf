package org.telekit.base.util;

import java.util.*;

public final class SequenceGenerator {

    private static final Map<String, String> REPLACEMENTS_CACHE = new HashMap<>();

    public static List<Map<String, String>> generate(List<Item> items) {
        REPLACEMENTS_CACHE.clear();

        List<Map<String, String>> sequence = new ArrayList<>();
        iterate(items, Objects.requireNonNull(nextItem(items, null)), sequence);

        return sequence;
    }

    private static Item nextItem(List<Item> items, Item currentItem) {
        if (currentItem == null) return items.get(0);
        int currentIndex = items.indexOf(currentItem);
        return currentIndex < items.size() - 1 ? items.get(currentIndex + 1) : null;
    }

    private static void iterate(List<Item> items, Item currentItem, List<Map<String, String>> accumulator) {
        double cursor = currentItem.start;
        Item nextItem = nextItem(items, currentItem);

        for (int i = 0; i < currentItem.count; i++) {
            REPLACEMENTS_CACHE.put(currentItem.id, String.valueOf((long) cursor));

            if (nextItem != null) {
                iterate(items, nextItem, accumulator);
            } else {
                accumulator.add(new HashMap<>(REPLACEMENTS_CACHE));
            }

            cursor = cursor + currentItem.step;
        }
    }

    public static class Item {

        public final String id;
        public final double start;
        public final int step;
        public final int count;

        public Item(String id, double start, int step, int count) {
            this.id = id;
            this.start = start;
            this.step = step;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return id.equals(item.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id='" + id + '\'' +
                    ", start=" + start +
                    ", step=" + step +
                    ", count=" + count +
                    '}';
        }
    }
}
