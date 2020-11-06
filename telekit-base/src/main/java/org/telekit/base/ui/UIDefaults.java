package org.telekit.base.ui;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.Env;

import java.awt.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.telekit.base.util.NumberUtils.ensureRange;

public interface UIDefaults {

    int TEXTAREA_ROW_LIMIT = 1000;

    int MAX_TASKBAR_WIDTH = 160;
    int MAX_TASKBAR_HEIGHT = 80;

    int MIN_WIDTH = 1024 - MAX_TASKBAR_WIDTH;
    int MIN_HEIGHT = 768 - MAX_TASKBAR_HEIGHT;
    int PREF_WIDTH = 1440 - MAX_TASKBAR_WIDTH;
    int PREF_HEIGHT = 900 - MAX_TASKBAR_HEIGHT;

    Dimension FORCED_WINDOW_SIZE = parseWindowsSize();

    private static @Nullable Dimension parseWindowsSize() {
        String property = Env.FORCED_WINDOWS_SIZE;
        if (isEmpty(property)) return null;

        String[] bounds = property.split("x");
        if (bounds.length != 2 || !NumberUtils.isDigits(bounds[0]) || !NumberUtils.isDigits(bounds[1])) {
            return null;
        }

        int userWidth = Integer.parseInt(bounds[0]);
        int userHeight = Integer.parseInt(bounds[1]);

        // be sensible
        userWidth = ensureRange(userWidth, 256, 4096, PREF_WIDTH);
        userHeight = ensureRange(userHeight, 256, 4096, PREF_HEIGHT);

        return new Dimension(userWidth, userHeight);
    }
}
