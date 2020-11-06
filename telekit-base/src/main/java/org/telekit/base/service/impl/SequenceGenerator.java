package org.telekit.base.service.impl;

import java.util.*;

public class SequenceGenerator {

    private final Map<String, String> cache = new HashMap<>();
    private final List<Item> items;

    public SequenceGenerator(List<Item> items) {
        this.items = items;
    }

    public List<Map<String, String>> generate() {
        List<Map<String, String>> sequence = new ArrayList<>();
        iterate(Objects.requireNonNull(nextItem(null)), sequence);
        return sequence;
    }

    private void iterate(Item currentItem, List<Map<String, String>> accumulator) {
        double cursor = currentItem.start;
        Item nextItem = nextItem(currentItem);

        for (int i = 0; i < currentItem.count; i++) {
            cache.put(currentItem.id, String.valueOf((long) cursor));

            if (nextItem != null) {
                iterate(nextItem, accumulator);
            } else {
                accumulator.add(new HashMap<>(cache));
            }

            cursor = cursor + currentItem.step;
        }
    }

    private Item nextItem(Item currentItem) {
        if (currentItem == null) return items.get(0);
        int currentIndex = items.indexOf(currentItem);
        return currentIndex < items.size() - 1 ? items.get(currentIndex + 1) : null;
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
