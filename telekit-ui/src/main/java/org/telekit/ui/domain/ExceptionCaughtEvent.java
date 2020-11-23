package org.telekit.ui.domain;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.event.Event;

import java.util.Objects;

public class ExceptionCaughtEvent extends Event {

    private final Throwable cause;

    public ExceptionCaughtEvent(Throwable cause) {
        this.cause = Objects.requireNonNull(cause);
    }

    public @NotNull Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "ExceptionCaughtEvent{" +
                "cause=" + cause +
                "} " + super.toString();
    }
}
