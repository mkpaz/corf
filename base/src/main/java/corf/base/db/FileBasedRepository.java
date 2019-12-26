package corf.base.db;

import org.jetbrains.annotations.Nullable;
import corf.base.exception.AppException;
import corf.base.i18n.M;
import corf.base.io.Serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static corf.base.i18n.I18n.t;

public abstract class FileBasedRepository<T extends Entity<T, ID>, ID extends Serializable>
        implements EntityRepository<T, ID> {

    protected Map<ID, T> repository = new HashMap<>();

    // always use single Transaction object (or pool) to avoid inner class memory leak
    private @Nullable Transaction transaction;

    @Override
    public List<T> getAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public long count() {
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
        Objects.requireNonNull(entity);
        addAll(List.of(entity));
    }

    @Override
    public ID addAndReturnId(T entity) {
        add(entity);
        return entity.getId();
    }

    @Override
    public void addAll(Collection<T> entities) {
        if (entities.isEmpty()) { return; }

        entities.forEach(entity -> {
            requireNotEmptyFields(entity);
            requireUniqueFields(entity);
        });

        for (T entity : entities) {
            repository.putIfAbsent(entity.getId(), entity.copy()); // ignore entities with duplicate ID
        }
    }

    @Override
    public void update(T entity) {
        requireNotEmptyFields(entity);
        repository.replace(entity.getId(), entity.copy());
    }

    @Override
    public void removeById(ID id) {
        repository.remove(id);
    }

    @Override
    public void removeAllById(Collection<ID> ids) {
        if (ids.isEmpty()) { return; }
        ids.forEach(Objects::requireNonNull);
        repository.keySet().removeAll(ids);
    }

    @Override
    public void remove(T entity) {
        removeById(entity.getId());
    }

    @Override
    public void removeAll(Collection<T> entities) {
        removeAllById(
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
        addAll(serializer.deserialize(inputStream));
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
        // so, you'll get IllegalArgumentException for free, but still it can be overridden
        Objects.requireNonNull(entity.getId());
    }

    protected void requireUniqueFields(T entity) {
        if (repository.containsKey(entity.getId())) {
            throw new AppException(t(M.MSG_KEY_IS_NOT_UNIQUE, "ID=" + entity.getId()));
        }
    }

    public class Transaction {

        private static final String TYPE_COLLECTION = "TYPE_COLLECTION";
        private static final String TYPE_ENTITY = "TYPE_ENTITY";

        private @Nullable Map<ID, T> backupCollection;
        private @Nullable T backupEntity;
        private @Nullable String type;

        @SuppressWarnings("unused")
        private void begin(boolean deepCopy) {
            resetPreviousBackup();

            // For now only shallow copy is supported.
            // Deep copy requires some third-party lib to simplify the task (e.g. Kryo).

            backupCollection = new HashMap<>(repository);
            backupEntity = null;
            type = TYPE_COLLECTION;
        }

        private void begin(T entity) {
            resetPreviousBackup();
            backupEntity = entity.copy();
            type = TYPE_ENTITY;
        }

        private void resetPreviousBackup() {
            backupCollection = null;
            backupEntity = null;
            type = null;
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
            Objects.requireNonNull(type, "type");

            switch (type) {
                case TYPE_COLLECTION -> {
                    Objects.requireNonNull(backupCollection, "backupCollection");
                    repository = backupCollection;
                }
                case TYPE_ENTITY -> {
                    if (backupEntity == null) { break; }

                    if (contains(backupEntity)) {
                        update(backupEntity);
                    } else {
                        add(backupEntity);
                    }
                }
            }
        }
    }
}
