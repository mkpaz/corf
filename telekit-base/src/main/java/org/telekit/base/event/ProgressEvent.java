package org.telekit.base.event;

// Notifies about some tasks progress or status
public class ProgressEvent extends Event {

    private final boolean active;

    public ProgressEvent(boolean active) {
        super();
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "ProgressIndicatorEvent{" +
                "active=" + active +
                "} " + super.toString();
    }
}