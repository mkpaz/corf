package telekit.desktop.event;

import org.jetbrains.annotations.Nullable;
import telekit.base.desktop.Dimension;
import telekit.base.event.Event;
import telekit.desktop.Launcher;

public final class CloseRequestEvent extends Event {

    private final int exitCode;
    private final Dimension windowSize;

    public CloseRequestEvent(Dimension windowSize) {
        this(Launcher.DEFAULT_EXIT_CODE, windowSize);
    }

    public CloseRequestEvent(int exitCode, Dimension windowSize) {
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
