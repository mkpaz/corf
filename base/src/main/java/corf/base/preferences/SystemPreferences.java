package corf.base.preferences;

import corf.base.Env;
import corf.base.desktop.Dimension;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.prefs.Preferences;

import static corf.base.Env.FULLSCREEN_SIZE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * This class uses {@link java.util.prefs.Preferences} to store user preferences
 * that are related to specific machine or host.
 */
@SuppressWarnings("unused")
public class SystemPreferences {

    private static final System.Logger LOGGER = System.getLogger(SystemPreferences.class.getName());
    private static final String KEY_WINDOW_WIDTH = "windowWidth";
    private static final String KEY_WINDOW_HEIGHT = "windowHeight";

    private final Preferences root;

    public SystemPreferences(Preferences root) {
        this.root = Objects.requireNonNull(root, "java.util.preferences");
    }

    public Preferences getUserRoot() {
        return root;
    }

    /** Returns stored window size. */
    public @Nullable Dimension getMainWindowSize() {
        try {
            int width = root.getInt(KEY_WINDOW_WIDTH, (int) FULLSCREEN_SIZE.width());
            int height = root.getInt(KEY_WINDOW_HEIGHT, (int) FULLSCREEN_SIZE.height());

            return new Dimension(width, height);
        } catch (Exception e) {
            LOGGER.log(WARNING, ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /** Stores window size. */
    public void setMainWindowSize(Dimension dimension) {
        try {
            Objects.requireNonNull(dimension, "dimension");

            int width = Math.max((int) dimension.width(), (int) Env.MIN_WINDOW_SIZE.width());
            int height = Math.max((int) dimension.height(), (int) Env.MIN_WINDOW_SIZE.height());

            if (FULLSCREEN_SIZE.equals(dimension)) {
                width = (int) FULLSCREEN_SIZE.width();
                height = (int) FULLSCREEN_SIZE.height();
            }

            root.putInt(KEY_WINDOW_WIDTH, width);
            root.putInt(KEY_WINDOW_HEIGHT, height);
        } catch (Exception e) {
            LOGGER.log(WARNING, ExceptionUtils.getStackTrace(e));
        }
    }
}

