package corf.base.db;

import java.util.function.Supplier;

public interface TransactionManager {

    void begin();

    void commit();

    void rollback();

    default void execute(Runnable runnable) {
        begin();
        boolean rollback = true;
        try {
            runnable.run();
            rollback = false;
            commit();
        } finally {
            if (rollback) {
                rollback();
            }
        }
    }

    default <T> T execute(Supplier<T> supplier) {
        begin();
        boolean rollback = true;
        try {
            T t = supplier.get();
            rollback = false;
            commit();
            return t;
        } finally {
            if (rollback) {
                rollback();
            }
        }
    }
}
