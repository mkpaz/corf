package org.telekit.desktop.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.event.Event;

import java.util.Objects;

public class ApplicationEvent extends Event {

    private final Type type;
    private final Object userData;

    public ApplicationEvent(Type type) {
        this(type, null);
    }

    public ApplicationEvent(Type type, Object userData) {
        this.type = Objects.requireNonNull(type);
        this.userData = userData;
    }

    public @NotNull Type getType() {
        return type;
    }

    public @Nullable Object getUserData() {
        return userData;
    }

    public enum Type {
        RESTART_REQUIRED,
        PREFERENCES_CHANGED,
        COMPLETION_REGISTRY_UPDATED;

        public boolean isSameTypeAs(ApplicationEvent event) {
            return event != null && event.getType() == this;
        }
    }

    @Override
    public String toString() {
        return "ApplicationEvent{" +
                "type=" + type +
                ", userData=" + userData +
                "} " + super.toString();
    }
}