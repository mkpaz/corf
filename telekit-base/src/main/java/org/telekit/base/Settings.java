package org.telekit.base;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.internal.UserPreferences;
import org.telekit.base.plugin.Plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.telekit.base.util.CommonUtils.defaultPath;
import static org.telekit.base.util.CommonUtils.getPropertyOrEnv;

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
    public static final boolean FORCE_RESIZE = getPropertyOrEnv("telekit.resizable", "TELEKIT_RESIZABLE") != null;

    public static Path getPluginDataDir(Class<? extends Plugin> clazz) {
        return DATA_DIR.resolve(clazz.getPackageName());
    }

    public static Image getIcon(String iconID) {
        return ICON_CACHE.get(iconID);
    }

    public static void putIcon(String iconID, Image icon) {
        ICON_CACHE.put(iconID, icon);
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
