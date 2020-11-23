package org.telekit.base.event;

import org.jetbrains.annotations.Nullable;

public class SubmitEvent<T> extends Event {

    private final T data;

    public SubmitEvent(T data) {
        this.data = data;
    }

    public @Nullable T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "SubmitEvent{" +
                "data=" + data +
                "} " + super.toString();
    }
}
