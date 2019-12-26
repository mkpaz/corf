package org.telekit.controls.util;

import javafx.concurrent.Task;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.event.DefaultEventBus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fluent wrapper for {@link Task} influenced by {@link CompletableFuture}.
 * The notable difference is that {@link Task} returns to the main/FX thread
 * automatically, so this helper is safe to be used from UI.
 */
public class Promise<T> {

    protected final Task<T> task;

    private Promise(Task<T> task) {
        this.task = task;
    }

    public static RunnablePromise runAsync(Runnable runnable) {
        return new RunnablePromise(runnable);
    }

    public static <T> SupplierPromise<T> supplyAsync(Supplier<T> supplier) {
        return new SupplierPromise<>(supplier);
    }

    public Promise<T> exceptionally(Consumer<Throwable> consumer) {
        task.setOnFailed(e -> {
            Throwable t = e.getSource().getException();
            if (t != null) { consumer.accept(t); }
        });
        return this;
    }

    public Promise<T> printOnException() {
        return exceptionally(Throwable::printStackTrace);
    }

    public Promise<T> notifyOnException() {
        return exceptionally(t -> DefaultEventBus.getInstance().publish(Notification.error(t)));
    }

    public void start() {
        new Thread(task).start();
    }

    public void start(ExecutorService service) {
        service.submit(task);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class RunnablePromise extends Promise<Void> {

        public RunnablePromise(Runnable runnable) {
            super(new Task<>() {
                @Override
                protected Void call() {
                    runnable.run();
                    return null;
                }
            });
        }

        public RunnablePromise then(Runnable runnable) {
            task.setOnSucceeded(e -> runnable.run());
            task.setOnCancelled(e -> runnable.run());
            return this;
        }
    }

    public static class SupplierPromise<T> extends Promise<T> {

        public SupplierPromise(Supplier<T> supplier) {
            super(new Task<>() {
                @Override
                protected T call() {
                    return supplier.get();
                }
            });
        }

        public SupplierPromise<T> then(Consumer<T> consumer) {
            task.setOnSucceeded(e -> consumer.accept(task.getValue()));
            task.setOnCancelled(e -> consumer.accept(task.getValue()));
            return this;
        }
    }
}
