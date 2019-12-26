package org.telekit.base.domain.event;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.event.Event;

import java.util.Objects;

public final class Notification extends Event {

    public enum Type {
        INFO, WARNING, ERROR
    }

    private final Type type;
    private final String text;
    private final Throwable throwable;

    private Notification(Type type, String text, Throwable throwable) {
        this.type = Objects.requireNonNull(type);
        this.text = Objects.requireNonNull(text);
        this.throwable = throwable;
    }

    public Type getType() { return type; }

    public String getText() { return text; }

    public @Nullable Throwable getThrowable() { return throwable; }

    public Notification info(String text) {
        return new Notification(Type.INFO, text, null);
    }

    public static Notification warning(String text) {
        return new Notification(Type.WARNING, text, null);
    }

    public static Notification error(Throwable t) {
        return error(t.getMessage(), t);
    }

    public static Notification error(String text, Throwable t) {
        return new Notification(Type.ERROR, text, t);
    }
}
