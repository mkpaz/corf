package org.telekit.base.service;

import org.telekit.base.domain.Entity;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.i18n.I18n;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.telekit.base.i18n.BaseMessages.MSG_KEY_IS_NOT_UNIQUE;

public abstract class FileBasedRepository<T extends Entity<T, ID>, ID extends Serializable>
        implements EntityRepository<T, ID> {

    protected Map<ID, T> repository = new HashMap<>();
    private Transaction transaction; // always use single Transaction object (or pool) to avoid inner class memory leak

    @Override
    public List<T> getAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public int size() {
        return repository.size();
    }

    @Override
    public Optional<T> find(T entity) {
        return findById(entity.getId());
    }

    @Override
    public boolean contains(T entity) {
        return containsId(entity.getId());
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(repository.get(id));
    }

    @Override
    public boolean containsId(ID id) {
        return repository.containsKey(id);
    }

    @Override
    public void add(T entity) {
        add(List.of(entity));
    }

    @Override
    public void add(Collection<T> entities) {
        if (entities.isEmpty()) { return; }

        entities.forEach(entity -> {
            requireNotEmptyFields(entity);
            requireUniqueFields(entity);
        });

        for (T entity : entities) {
            repository.putIfAbsent(entity.getId(), entity.deepCopy()); // ignore entities with duplicate ID
        }
    }

    @Override
    public void update(T entity) {
        requireNotEmptyFields(entity);
        repository.replace(entity.getId(), entity.deepCopy());
    }

    @Override
    public void removeById(ID id) {
        repository.remove(id);
    }

    @Override
    public void removeById(Collection<ID> ids) {
        if (ids.isEmpty()) { return; }
        ids.forEach(Objects::requireNonNull);
        repository.keySet().removeAll(ids);
    }

    @Override
    public void remove(T entity) {
        removeById(entity.getId());
    }

    @Override
    public void remove(Collection<T> entities) {
        removeById(
                entities.stream()
                        .map(Entity::getId)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void clear() {
        repository = new HashMap<>();
    }

    public void load(InputStream inputStream, Serializer<Collection<T>> serializer) {
        add(serializer.deserialize(inputStream));
    }

    public void save(OutputStream outputStream, Serializer<Collection<T>> serializer) {
        serializer.serialize(outputStream, getAll());
    }

    public Transaction beginTransaction(boolean deepCopy) {
        transaction = transaction == null ? new Transaction() : transaction;
        transaction.begin(deepCopy);
        return transaction;
    }

    public Transaction beginTransaction(T entity) {
        transaction = transaction == null ? new Transaction() : transaction;
        transaction.begin(entity);
        return transaction;
    }

    protected void requireNotEmptyFields(T entity) {
        // this check is somewhat redundant, because all constructors and setters mark ID with annotation
        // so you'll get IllegalArgumentException for free, but still it can be overridden
        Objects.requireNonNull(entity.getId());
    }

    protected void requireUniqueFields(T entity) {
        if (repository.containsKey(entity.getId())) {
            throw new TelekitException(I18n.t(MSG_KEY_IS_NOT_UNIQUE, "ID=" + entity.getId()));
        }
    }

    public class Transaction {

        private static final String TYPE_COLLECTION = "TYPE_COLLECTION";
        private static final String TYPE_ENTITY = "TYPE_ENTITY";

        private Map<ID, T> backupCollection;
        private T backupEntity;
        private String type;

        private void begin(boolean deepCopy) {
            resetPreviousBackup();
            // TODO: create deep copy before transaction (Kryo?)
            backupCollection = new HashMap<>(repository);
            backupEntity = null;
            type = TYPE_COLLECTION;
        }

        private void begin(T entity) {
            resetPreviousBackup();
            backupEntity = entity.deepCopy();
            type = TYPE_ENTITY;
        }

        private void resetPreviousBackup() {
            backupCollection = null;
            backupEntity = null;
        }

        public void rollbackOnException(Runnable runnable) {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                doRollback();
                throw throwable;
            }
        }

        public void rollback() {
            doRollback();
        }

        private void doRollback() {
            switch (type) {
                case TYPE_COLLECTION:
                    repository = backupCollection;
                    break;
                case TYPE_ENTITY:
                    if (contains(backupEntity)) {
                        update(backupEntity);
                    } else {
                        add(backupEntity);
                    }
                    break;
            }
        }
    }
}
