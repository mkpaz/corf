package org.telekit.base;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.util.DesktopUtils;

import java.awt.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.service.Encryptor.Algorithm;
import static org.telekit.base.ui.UIDefaults.PREF_HEIGHT;
import static org.telekit.base.ui.UIDefaults.PREF_WIDTH;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.base.util.FileUtils.ensureNotNull;
import static org.telekit.base.util.FileUtils.urlToFile;
import static org.telekit.base.util.NumberUtils.ensureRange;

public final class Env {

    public static final String APP_NAME = "Telekit";
    public static final String AUTOCOMPLETE_DIR_NAME = "autocomplete";
    public static final String CONFIG_DIR_NAME = "config";
    public static final String CACHE_DIR_NAME = "cache";
    public static final String DATA_DIR_NAME = "data";
    public static final String DOCS_DIR_NAME = "docs";
    public static final String DOCS_INDEX_FILE_NAME = "index";
    public static final String LIB_DIR_NAME = "lib";
    public static final String PLUGINS_DIR_NAME = "plugins";
    public static final Algorithm DEFAULT_ENCRYPTION_ALG = Algorithm.AES_GCM;

    /**
     * This is root application directory. In other words, this is where app was run from.
     * There is no reliable way to determine that directory at Java runtime, so it should
     * be set as env variable or system property in app launcher (binary or shell script).
     * If none of them were set, we fallback to the {@code getProtectionDomain().getCodeSource()},
     * which is not reliable. The latter should only happen in unit tests, so don't really
     * matter.
     */
    public static final @NotNull Path APP_DIR = findAppDir();

    /**
     * This is where all application data (both config and plugins) are stored. We use single
     * directory for all kinds of app data because not every OS has a notion of "config" and
     * "data" dirs. Overall data dir is supposed to look like this:
     * <pre>
     * |-- autocomplete
     * |-- cache
     * |-- config
     *     |-- foo.yaml
     *     |-- bar.yaml
     * |-- plugins
     *     |-- org.example.FooPlugin
     *         |-- config
     *             |-- baz.yaml
     *         |-- docs
     *         |-- lib
     *             |-- plugin.jar
     * |-- logging.properties
     * </pre>
     */
    public static final @NotNull Path DATA_DIR = findDataDir();

    /** This is where all application configs are stored. */
    public static final @NotNull Path CONFIG_DIR = DATA_DIR.resolve(CONFIG_DIR_NAME);

    /** Documentation is updated with the application, so it resides in the app dir. */
    public static final @NotNull Path DOCS_DIR = APP_DIR.resolve(DOCS_DIR_NAME);

    public static final @NotNull Path AUTOCOMPLETE_DIR = DATA_DIR.resolve(AUTOCOMPLETE_DIR_NAME);
    public static final @NotNull Path CACHE_DIR = DATA_DIR.resolve(CACHE_DIR_NAME);
    public static final @NotNull Path LOGS_DIR = APP_DIR;
    public static final @NotNull Path PLUGINS_DIR = DATA_DIR.resolve(PLUGINS_DIR_NAME);

    public static final @NotNull Path HOME_DIR = findHomeDir();
    public static final @NotNull Path TEMP_DIR = findTempDir();

    public static @NotNull Path findAppDir() {
        // normally, env var or property is ALWAYS set, except for unit tests
        String envValue = getPropertyOrEnv("telekit.app.dir", "TELEKIT_APP_DIR");
        if (envValue != null && !envValue.isBlank()) return Paths.get(envValue);

        try {
            URL url = Env.class.getProtectionDomain().getCodeSource().getLocation();
            if (url != null) return urlToFile(url).toPath();
        } catch (Exception ignored) {}

        // just a formality to avoid NPE in any case
        return findTempDir().resolve(APP_NAME.toLowerCase());
    }

    public static @NotNull Path findDataDir() {
        String envValue = getPropertyOrEnv("telekit.data.dir", "TELEKIT_DATA_DIR");

        // store data in the user home directory
        if ("$HOME".equalsIgnoreCase(envValue)) {
            if (SystemUtils.IS_OS_UNIX) return DesktopUtils.getXdgConfigDir().resolve(APP_NAME);
            if (SystemUtils.IS_OS_WINDOWS) return DesktopUtils.getLocalAppDataDir().resolve(APP_NAME);
        }

        // save data in user specified directory
        if (envValue != null && !envValue.isBlank()) return Paths.get(envValue);

        // store data in the program installation directory
        return APP_DIR.resolve(DATA_DIR_NAME);
    }

    public static @NotNull Path findHomeDir() {
        // ensureNotNull() is just a formality to avoid NPE in any case
        return ensureNotNull(System.getProperty("user.home"), Paths.get("home"));
    }

    public static @NotNull Path findTempDir() {
        // ensureNotNull() is just a formality to avoid NPE in any case
        return ensureNotNull(System.getProperty("java.io.tmpdir"), Paths.get("tmp"));
    }

    public static @NotNull Path getPluginDataDir(Class<? extends Plugin> pluginClass) {
        return PLUGINS_DIR.resolve(className(pluginClass));
    }

    public static @NotNull Path getPluginConfigDir(Class<? extends Plugin> pluginClass) {
        return getPluginDataDir(pluginClass).resolve(CONFIG_DIR_NAME);
    }

    public static @NotNull Path getPluginDocsDir(Class<? extends Plugin> pluginClass) {
        return getPluginDataDir(pluginClass).resolve(DOCS_DIR_NAME);
    }

    public static @NotNull Path getPluginLibDir(Class<? extends Plugin> pluginClass) {
        return getPluginDataDir(pluginClass).resolve(LIB_DIR_NAME);
    }

    public static @Nullable String getPropertyOrEnv(String propertyKey, String envKey) {
        return System.getProperty(propertyKey, System.getenv(envKey));
    }

    public static final @Nullable Locale LOCALE = getLocaleFromEnv();

    public static @Nullable Locale getLocaleFromEnv() {
        String localeStr = getPropertyOrEnv("telekit.language", "TELEKIT_LANGUAGE");
        if (isNotBlank(localeStr)) {
            Locale locale = new Locale.Builder().setLanguageTag(localeStr).build();
            if (LocaleUtils.isAvailableLocale(locale)) {
                return locale;
            }
        }
        return null;
    }

    public static final Dimension FORCED_WINDOW_SIZE = parseWindowsSize();

    private static @Nullable Dimension parseWindowsSize() {
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

    // TODO: Find a better way to provide app version. This way it can be manipulated at runtime.
    public static @Nullable String getAppVersion() {
        return System.getProperty("application.version");
    }
}
