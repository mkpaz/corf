package corf.base.event;

import backbonefx.event.AbstractEvent;
import backbonefx.event.EventSource;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ActionEvent<T> extends AbstractEvent {

    private final String action;
    private final @Nullable T payload;

    public ActionEvent(String action) {
        this(action, null, null);
    }

    public ActionEvent(String action, @Nullable EventSource source) {
        this(action, source, null);
    }

    public ActionEvent(String action, @Nullable EventSource source, @Nullable T payload) {
        super(source);
        this.action = Objects.requireNonNull(action, "action");
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public @Nullable T getPayload() {
        return payload;
    }

    public boolean matches(String action) {
        return Objects.equals(this.action, action);
    }

    public boolean matches(String action, @Nullable EventSource source) {
        return Objects.equals(this.action, action) && isSentBy(source);
    }
}
