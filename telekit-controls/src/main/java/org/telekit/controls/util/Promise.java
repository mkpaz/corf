package org.telekit.controls.util;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.event.DefaultEventBus;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fluent wrapper for {@link Task} influenced by {@link CompletableFuture}.
 * The notable difference is that {@link Task} returns to the main/FX thread
 * automatically, so this helper is safe to be used from UI.
 * <p>
 * Note that {@link ExecutorService} and also {@link FutureTask} swallows any exception.
 * While {@link ExecutorService} constructors accept {@link ThreadFactory}, so we can
 * instantiate threads with custom {@link UncaughtExceptionHandler}, {@link FutureTask}
 * has no such feature. That's why {@link Task#setOnFailed(EventHandler)} is explicitly
 * called in constructor. You can override default notification handler with custom
 * implementation by calling {@link #exceptionally(Consumer)}.
 */
public class Promise<T> {

    protected final Task<T> task;

    private Promise(Task<T> task) {
        this.task = task;
        notifyOnException();
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
