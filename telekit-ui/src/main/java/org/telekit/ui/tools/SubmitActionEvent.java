package org.telekit.ui.tools;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.event.SubmitEvent;

import java.util.Objects;

public class SubmitActionEvent<T> extends SubmitEvent<T> {

    private final Action action;

    public SubmitActionEvent(T data, Action action) {
        super(data);
        this.action = Objects.requireNonNull(action);
    }

    public @NotNull Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "SubmitActionEvent{" +
                "action=" + action +
                "} " + super.toString();
    }
}
