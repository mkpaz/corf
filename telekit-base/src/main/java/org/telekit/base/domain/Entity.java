package org.telekit.base.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Entity<T extends Entity<T, ID>, ID> implements Comparable<T> {

    private ID id;

    public Entity() {}

    public Entity(ID id) {
        this.id = Objects.requireNonNull(id);
    }

    public Entity(T that) {
        this.id = Objects.requireNonNull(that).getId();
    }

    public @NotNull ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Entity<?, ?> that = (Entity<?, ?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull Entity that) {
        Objects.requireNonNull(that);
        if (this == that) { return 0; }
        return String.valueOf(id).compareTo(String.valueOf(that.getId()));
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                '}';
    }

    public abstract T deepCopy();
}
