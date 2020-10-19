package org.telekit.base.service;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

public interface EntityRepository<T, ID extends Serializable> extends Repository<T> {

    Optional<T> findById(@NotNull ID id);

    boolean containsId(@NotNull ID id);

    void removeById(@NotNull ID id);

    void removeById(@NotNull Collection<ID> ids);
}
