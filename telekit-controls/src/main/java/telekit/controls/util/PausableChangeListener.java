package telekit.controls.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.Consumer;

public class PausableChangeListener<T> implements ChangeListener<T> {

    protected final Consumer<T> consumer;
    protected boolean active = true;

    public PausableChangeListener(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void changed(ObservableValue<? extends T> obs, T old, T value) {
        if (active) { consumer.accept(value); }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void pauseAndRun(Runnable runnable) {
        try {
            active = false;
            runnable.run();
        } finally {
            active = true;
        }
    }
}
