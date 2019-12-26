package corf.base.desktop;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple wrapper for starting {@link Task}'s in fluent style.
 * For more complex uses cases stick to standard API.
 */
public final class Async<V> {

    private final AsyncTask<V> task;

    private Async(AsyncTask<V> task) {
        this.task = Objects.requireNonNull(task, "task");
    }

    public Async<V> setOnScheduled(Runnable runnable) {
        task.setOnScheduled(e -> runnable.run());
        return this;
    }

    public Async<V> setOnRunning(Runnable runnable) {
        task.setOnRunning(e -> runnable.run());
        return this;
    }

    @SuppressWarnings("unchecked")
    public Async<V> setOnSucceeded(Consumer<V> consumer) {
        task.setOnSucceeded(e -> {
            Worker<V> worker = e.getSource();
            consumer.accept(worker.getValue());
        });
        return this;
    }

    public Async<V> setOnCancelled(Runnable runnable) {
        task.setOnCancelled(e -> runnable.run());
        return this;
    }

    @SuppressWarnings("unchecked")
    public Async<V> setOnFinished(Consumer<V> consumer) {
        task.setOnSucceeded(e -> {
            Worker<V> worker = e.getSource();
            consumer.accept(worker.getValue());
        });
        task.setOnCancelled(e -> consumer.accept(null));
        return this;
    }

    @SuppressWarnings("unchecked")
    public Async<V> setOnFailed(Consumer<Throwable> consumer) {
        task.setOnFailed(e -> {
            Worker<V> worker = e.getSource();
            consumer.accept(worker.getException());
        });
        return this;
    }

    public void start() {
        new Thread(task).start();
    }

    public void start(Executor executor) {
        executor.execute(task);
    }

    public static Async<Void> with(Runnable runnable) {
        return new Async<>(new AsyncTask<>(() -> {
            runnable.run();
            return null;
        }));
    }

    public static <V> Async<V> with(Supplier<V> supplier) {
        return new Async<>(new AsyncTask<>(supplier));
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class AsyncTask<V> extends Task<V> {

        private final Supplier<V> supplier;

        public AsyncTask(Supplier<V> supplier) {
            super();
            this.supplier = supplier;
        }

        @Override
        protected @Nullable V call() {
            return supplier.get();
        }
    }
}
