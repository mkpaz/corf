package org.telekit.base;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.plugin.Plugin;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.LauncherDefaults.PREF_HEIGHT;
import static org.telekit.base.LauncherDefaults.PREF_WIDTH;
import static org.telekit.base.util.CommonUtils.defaultPath;
import static org.telekit.base.util.CommonUtils.getPropertyOrEnv;
import static org.telekit.base.util.NumberUtils.ensureRange;

public final class Environment {

    public static final String APP_NAME = "Telekit";
    public static final int TEXTAREA_ROW_LIMIT = 1000;

    /* Application Paths */

    public static final Path APP_DIR = Paths.get(
            getPropertyOrEnv("telekit.app.dir", "TELEKIT_APP_DIR")
    );
    public static final Path DATA_DIR = defaultPath(
            getPropertyOrEnv("telekit.data.dir", "TELEKIT_DATA_DIR"), APP_DIR.resolve("data")
    );
    public static final Path PLUGINS_DIR = defaultPath(
            getPropertyOrEnv("telekit.plugins.dir", "TELEKIT_PLUGINS_DIR"), APP_DIR.resolve("plugins")
    );
    public static final Path DOCS_DIR = APP_DIR.resolve("docs");
    public static final Path HOME_DIR = Paths.get(System.getProperty("user.home"));
    public static final Path OS_TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    public static final Path APP_TEMP_DIR = OS_TEMP_DIR.resolve("telekit");

    public static Path getPluginResourcesDir(Class<? extends Plugin> clazz) {
        return DATA_DIR.resolve(clazz.getPackageName());
    }

    /* Screen Size */

    public static final Dimension FORCE_WINDOW_SIZE = getForcedWindowsSize();

    @Nullable
    private static Dimension getForcedWindowsSize() {
        String property = getPropertyOrEnv("telekit.window.size", "TELEKIT_WINDOW_SIZE");
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

    /* Locale */

    public static Locale LOCALE = getLocaleFromEnv();

    private static Locale getLocaleFromEnv() {
        String localeStr = getPropertyOrEnv("telekit.language", "TELEKIT_LANGUAGE");
        if (isNotBlank(localeStr)) {
            Locale locale = new Locale.Builder().setLanguageTag(localeStr).build();
            if (LocaleUtils.isAvailableLocale(locale)) {
                return locale;
            }
        }
        return null;
    }
}
