package org.telekit.base.event;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.event.Event;

import java.util.Objects;
import java.util.UUID;

public class ProgressIndicatorEvent extends Event {

    private final String id;
    private final boolean active;

    public ProgressIndicatorEvent(boolean active) {
        this(UUID.randomUUID().toString(), active);
    }

    public ProgressIndicatorEvent(String id, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.active = active;
    }

    public @NotNull String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "ProgressIndicatorEvent{" +
                "id='" + id + '\'' +
                ", active=" + active +
                "} " + super.toString();
    }
}