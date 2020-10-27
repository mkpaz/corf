package org.telekit.ui.domain;

public class ExceptionCaughtEvent {

    private final Throwable cause;

    public ExceptionCaughtEvent(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}
