package org.telekit.base.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public interface EntityRepository<T, ID extends Serializable> extends Repository<T> {

    Optional<T> findById(ID id);

    void removeById(ID id);

    default boolean containsId(ID id) {
        return findById(id).isPresent();
    }

    default void removeAllById(Collection<ID> c) {
        if (isNotEmpty(c)) {
            c.forEach(this::removeById);
        }
    }
}
