package org.telekit.ui.domain;

public class CloseEvent {

    private final int exitCode;

    public CloseEvent(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
