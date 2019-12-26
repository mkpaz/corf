package org.telekit.base.domain;

public class ProgressIndicatorEvent {

    private final String id;
    private final boolean active;

    public ProgressIndicatorEvent(String id, boolean active) {
        this.active = active;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }
}