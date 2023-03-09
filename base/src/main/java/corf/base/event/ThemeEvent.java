package corf.base.event;

import atlantafx.base.theme.Theme;
import backbonefx.event.Event;

import java.util.Objects;

public final class ThemeEvent implements Event {

    private final Theme theme;

    public ThemeEvent(Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public String toString() {
        return "ThemeChangeEvent{" +
                "theme=" + theme +
                '}';
    }
}
