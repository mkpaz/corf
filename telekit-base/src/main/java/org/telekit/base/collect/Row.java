package org.telekit.base.collect;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Row extends ArrayList<String> {

    private final List<String> data;

    public Row() {
        this.data = new ArrayList<>();
    }

    public Row(@NotNull Collection<? extends String> c) {
        this.data = new ArrayList<>(c);
    }

    public Row(int initialCapacity) {
        this.data = new ArrayList<>(initialCapacity);
    }

    @Override
    public boolean add(String s) {
        return data.add(s);
    }

    @Override
    public void add(int index, String element) {
        data.add(index, element);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return data.iterator();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public boolean remove(Object o) {
        return data.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends String> c) {
        return data.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends String> c) {
        return data.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return data.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return data.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<String> operator) {
        data.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super String> c) {
        data.sort(c);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Row row = (Row) o;
        return data.equals(row.data);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String get(int index) {
        return data.get(index);
    }

    @Override
    public String set(int index, String element) {
        return data.set(index, element);
    }

    @Override
    public String remove(int index) {
        return data.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    @Override
    public @NotNull ListIterator<String> listIterator() {
        return data.listIterator();
    }

    @Override
    public @NotNull ListIterator<String> listIterator(int index) {
        return data.listIterator(index);
    }

    @Override
    public @NotNull Row subList(int fromIndex, int toIndex) {
        return new Row(data.subList(fromIndex, toIndex));
    }

    @Override
    public Spliterator<String> spliterator() {
        return data.spliterator();
    }

    public static <E> List<E> copyOf(Collection<? extends E> coll) {
        return List.copyOf(coll);
    }

    @Override
    public boolean removeIf(Predicate<? super String> filter) {
        return data.removeIf(filter);
    }

    @Override
    public Stream<String> stream() {
        return data.stream();
    }

    @Override
    public Stream<String> parallelStream() {
        return data.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        data.forEach(action);
    }
}
