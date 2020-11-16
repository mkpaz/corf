package org.telekit.base.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Simple convenient wrapper around TreeMap to reflect menu or file catalog hierarchy,
 * and other structures that usually don't contain large amount of elements.
 */
public class SortedTreeNode<K, V> implements Iterable<SortedTreeNode<K, V>> {

    private final K key;
    private V value;
    private final SortedMap<K, SortedTreeNode<K, V>> children;

    public SortedTreeNode(K key) {
        this(key, null, null);
    }

    public SortedTreeNode(K key, V value) {
        this(key, value, null);
    }

    public SortedTreeNode(K key, V value, Comparator<K> comparator) {
        this.key = Objects.requireNonNull(key);
        this.value = value;
        this.children = new TreeMap<>(comparator);
    }

    public @NotNull K getKey() {
        return key;
    }

    public @Nullable V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public int capacity() {
        return children.size();
    }

    public Collection<SortedTreeNode<K, V>> getChildren() {
        return children.values();
    }

    public boolean contains(K key) {
        return children.containsKey(key);
    }

    public SortedTreeNode<K, V> get(K key) {
        return children.get(key);
    }

    public void put(SortedTreeNode<K, V> node) {
        children.put(node.getKey(), node);
    }

    public void putIfAbsent(SortedTreeNode<K, V> node) {
        children.putIfAbsent(node.getKey(), node);
    }

    public SortedTreeNode<K, V> computeIfAbsent(
            K key,
            Function<? super K, ? extends SortedTreeNode<K, V>> mappingFunction) {
        return children.computeIfAbsent(key, mappingFunction);
    }

    public void remove(K key) {
        children.remove(key);
    }

    @Override
    public @NotNull Iterator<SortedTreeNode<K, V>> iterator() {
        return children.values().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SortedTreeNode<?, ?> that = (SortedTreeNode<?, ?>) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
