package org.telekit.ui.domain;

import org.telekit.controls.domain.Dimension;

public class CloseEvent {

    private Dimension windowSize;
    private final int exitCode;

    public CloseEvent(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Dimension getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Dimension windowSize) {
        this.windowSize = windowSize;
    }
}
