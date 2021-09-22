package org.telekit.base.preferences;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.Dimension;

import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static org.telekit.base.Env.WINDOW_MAXIMIZED;

public class SystemPreferences {

    private static final Logger LOG = Logger.getLogger(SystemPreferences.class.getName());
    private static final String WINDOW_WIDTH = "windowWidth";
    private static final String WINDOW_HEIGHT = "windowHeight";

    private final Preferences root;

    public SystemPreferences(Preferences root) {
        this.root = Objects.requireNonNull(root);
    }

    public Preferences getUserRoot() {
        return root;
    }

    public @Nullable Dimension getMainWindowSize() {
        try {
            int width = root.getInt(WINDOW_WIDTH, (int) WINDOW_MAXIMIZED.width());
            int height = root.getInt(WINDOW_HEIGHT, (int) WINDOW_MAXIMIZED.height());

            return new Dimension(width, height);
        } catch (Exception e) {
            LOG.warning(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public void setMainWindowSize(Dimension dimension) {
        try {
            Objects.requireNonNull(dimension);

            int width = Math.max((int) dimension.width(), 800);
            int height = Math.max((int) dimension.height(), 600);

            if (WINDOW_MAXIMIZED.equals(dimension)) {
                width = (int) WINDOW_MAXIMIZED.width();
                height = (int) WINDOW_MAXIMIZED.height();
            }

            root.putInt(WINDOW_WIDTH, width);
            root.putInt(WINDOW_HEIGHT, height);
        } catch (Exception e) {
            LOG.warning(ExceptionUtils.getStackTrace(e));
        }
    }
}
