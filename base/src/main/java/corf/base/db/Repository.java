package corf.base.db;

import java.util.Collection;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public interface Repository<T> {

    Collection<T> getAll();

    long count();

    Optional<T> find(T entity);

    void add(T entity);

    void update(T entity);

    void remove(T entity);

    void clear();

    default boolean contains(T entity) {
        return find(entity).isPresent();
    }

    default void addAll(Collection<T> c) {
        if (isNotEmpty(c)) {
            c.forEach(this::add);
        }
    }

    default void removeAll(Collection<T> c) {
        if (isNotEmpty(c)) {
            c.forEach(this::remove);
        }
    }
}
