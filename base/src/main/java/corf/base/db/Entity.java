package corf.base.db;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Entity<T extends Entity<T, ID>, ID> implements Comparable<T> {

    private ID id;

    @SuppressWarnings("NullAway")
    public Entity() {
        // Allow null in the default constructor for serialization.
        // Implementations are free to override this, if necessary.
    }

    public Entity(ID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public Entity(T that) {
        this.id = that.getId();
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public abstract T copy();

    @Override
    @SuppressWarnings("EqualsGetClass")
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
}
