package org.telekit.base;

import javafx.scene.image.Image;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.internal.UserPreferences;
import org.telekit.base.plugin.Plugin;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.telekit.base.LauncherDefaults.PREF_HEIGHT;
import static org.telekit.base.LauncherDefaults.PREF_WIDTH;
import static org.telekit.base.util.CommonUtils.defaultPath;
import static org.telekit.base.util.CommonUtils.getPropertyOrEnv;
import static org.telekit.base.util.NumberUtils.ensureRange;

public final class Settings {

    public static final String APP_NAME = "Telekit";

    // icon cache
    public static final Map<String, Image> ICON_CACHE = new HashMap<>();
    public static final String ICON_APP = "ICON_APP";

    // application paths
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
    public static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    public static final boolean FORCE_WINDOW_RESIZE =
            getPropertyOrEnv("telekit.window.resizable", "TELEKIT_WINDOW_RESIZABLE") != null;
    public static final Dimension FORCE_WINDOW_SIZE = getForcedWindowsSize();
    public static final String PROXY_URL =
            getPropertyOrEnv("telekit.proxy.url", "TELEKIT_PROXY_URL");
    public static final String PROXY_USERNAME =
            getPropertyOrEnv("telekit.proxy.username", "TELEKIT_PROXY_USERNAME");
    public static final String PROXY_PASSWORD =
            getPropertyOrEnv("telekit.proxy.password", "TELEKIT_PROXY_PASSWORD");

    public static Path getPluginDataDir(Class<? extends Plugin> clazz) {
        return DATA_DIR.resolve(clazz.getPackageName());
    }

    public static Image getIcon(String iconID) {
        return ICON_CACHE.get(iconID);
    }

    public static void putIcon(String iconID, Image icon) {
        ICON_CACHE.put(iconID, icon);
    }

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

    ///////////////////////////////////////////////////////////////////////////

    private UserPreferences preferences;

    @NotNull
    public UserPreferences getPreferences() {
        return preferences != null ? preferences : new UserPreferences();
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }
}
