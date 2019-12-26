package corf.base.common;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class KeyValue<K, V> {

    private final K key;
    private final @Nullable V value;

    public KeyValue(K key, @Nullable V value) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public @Nullable V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        KeyValue<?, ?> keyValue = (KeyValue<?, ?>) o;

        if (!key.equals(keyValue.key)) { return false; }
        return Objects.equals(value, keyValue.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    public static <K, V> KeyValue<K, V> of(K key, @Nullable V value) {
        return new KeyValue<>(key, value);
    }
}
