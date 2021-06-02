package org.telekit.desktop.domain;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.event.Event;
import org.telekit.base.desktop.Dimension;

public class CloseEvent extends Event {

    private final int exitCode;
    private final Dimension windowSize;

    public CloseEvent(int exitCode) {
        this(exitCode, null);
    }

    public CloseEvent(int exitCode, Dimension windowSize) {
        this.exitCode = exitCode;
        this.windowSize = windowSize;
    }

    public int getExitCode() {
        return exitCode;
    }

    public @Nullable Dimension getWindowSize() {
        return windowSize;
    }

    @Override
    public String toString() {
        return "CloseEvent{" +
                "windowSize=" + windowSize +
                ", exitCode=" + exitCode +
                "} " + super.toString();
    }
}
