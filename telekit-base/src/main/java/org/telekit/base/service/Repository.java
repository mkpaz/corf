package org.telekit.base.service;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T> {

    Collection<T> getAll();

    int count();

    Optional<T> find(T entity);

    boolean contains(T entity);

    void add(T entity);

    void add(Collection<T> entity);

    void update(T entity);

    void remove(T entity);

    void remove(Collection<T> entities);

    void clear();
}
