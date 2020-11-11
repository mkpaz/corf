package org.telekit.base.ui;

import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface UIDefaults {

    int TEXTAREA_ROW_LIMIT = 1000;

    int MAX_TASKBAR_WIDTH = 160;
    int MAX_TASKBAR_HEIGHT = 80;

    int MIN_WIDTH = 1024 - MAX_TASKBAR_WIDTH;
    int MIN_HEIGHT = 768 - MAX_TASKBAR_HEIGHT;
    int PREF_WIDTH = 1440 - MAX_TASKBAR_WIDTH;
    int PREF_HEIGHT = 900 - MAX_TASKBAR_HEIGHT;

    // it's not immutable, so be careful
    Dimension WINDOW_MAXIMIZED = new Dimension(0, 0);

    static @NotNull Dimension getWindowSize(Stage stage) {
        return stage != null && !stage.isMaximized() ?
                new Dimension((int) stage.getWidth(), (int) stage.getHeight()) :
                WINDOW_MAXIMIZED;
    }
}
