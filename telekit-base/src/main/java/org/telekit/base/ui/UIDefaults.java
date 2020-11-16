package org.telekit.base.ui;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.telekit.controls.domain.Dimension;

public interface UIDefaults {

    int TEXTAREA_ROW_LIMIT = 1000;

    // the size of any window should be less than its parent size by that value
    int WINDOW_DELTA = 200;

    // the parent of main window is OS screen, so it should be less than standard screen resolution size
    Dimension MAIN_WINDOW_MIN_SIZE = Dimension.of(1024 - WINDOW_DELTA, 768 - WINDOW_DELTA);
    Dimension MAIN_WINDOW_PREF_SIZE = Dimension.of(1440 - WINDOW_DELTA, 900 - WINDOW_DELTA);

    // max dialog (alert) size
    Dimension DIALOG_MAX_SIZE = Dimension.of(500, MAIN_WINDOW_MIN_SIZE.getHeight() - WINDOW_DELTA);

    // special value that means window is maximized
    Dimension WINDOW_MAXIMIZED = Dimension.of(0, 0);

    default boolean isScreenFits(Dimension dimension) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() > dimension.getWidth() && screenBounds.getHeight() > dimension.getHeight();
    }

    static @NotNull Dimension getWindowSize(Stage stage) {
        return stage != null && !stage.isMaximized() ?
                new Dimension(stage.getWidth(), stage.getHeight()) :
                WINDOW_MAXIMIZED;
    }
}
