package org.telekit.base.service;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T> {

    Collection<T> getAll();

    int size();

    Optional<T> find(T entity);

    boolean contains(T entity);

    void add(@NotNull T entity);

    void add(@NotNull Collection<T> entity);

    void update(@NotNull T entity);

    void remove(@NotNull T entity);

    void remove(@NotNull Collection<T> entities);

    void clear();
}
