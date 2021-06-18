package org.telekit.base.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

public interface EntityRepository<T, ID extends Serializable> extends Repository<T> {

    Optional<T> findById(ID id);

    boolean containsId(ID id);

    void removeById(ID id);

    void removeById(Collection<ID> ids);
}
