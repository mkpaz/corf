package corf.base;

import corf.base.common.ClasspathResource;
import corf.base.desktop.Dimension;
import corf.base.desktop.OS;
import corf.base.io.FileSystemUtils;
import corf.base.plugin.Plugin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * Stores app wide constants and global resources that can be obtained
 * without the need to run the app. Must only contain static methods or variables.
 */
@SuppressWarnings("unused")
public final class Env {

    /** Default constructor. */
    private Env() { }

    /** Path to the base module for resolving module resources. */
    public static final ClasspathResource BASE_MODULE = ClasspathResource.of("/corf/base", Env.class);

    /** Application name. */
    public static final String APP_NAME = "Corf";

    /** Application project page. */
    public static final String APP_PROJECT_PAGE = "https://github.com/mkpaz/corf";

    /** Application icon. */
    public static final Image APP_ICON = new Image(Objects.requireNonNull(
            BASE_MODULE.concat("assets/app_64.png").getResourceAsStream()
    ));

    /** User home directory. */
    public static final Path HOME_DIR = findHomeDir();

    /** OS temp directory. */
    public static final Path TEMP_DIR = findTempDir();

    /** Application mode. Everything but "DEV" (including null) is considered as production mode. */
    public static final String MODE = getMode();

    /** Returns given system property or env variable if property value is null. */
    public static @Nullable String getPropertyOrEnv(String propertyKey, String envKey) {
        return System.getProperty(propertyKey, System.getenv(envKey));
    }

    /** Returns current app version (yes it uses system property, so it's deliberately not safe). */
    public static @Nullable String getAppVersion() {
        return System.getProperty("application.version");
    }

    /** See {@link #MODE}. */
    private static String getMode() {
        var mode = Env.getPropertyOrEnv("corf.mode", "CORF_MODE");
        return Objects.requireNonNullElse(mode, "WORK");
    }

    /** See {@link #MODE}. */
    public static boolean isDevMode() {
        return StringUtils.equalsIgnoreCase("DEV", MODE);
    }

    /**
     * Application language. Note that this value is only used for testing.
     * as application obtains locale from user preferences.
     */
    public static final @Nullable Locale LOCALE = getLocaleFromEnv();

    /** See {@link #LOCALE}. */
    public static @Nullable Locale getLocaleFromEnv() {
        String localeStr = getPropertyOrEnv("corf.language", "CORF_LANGUAGE");
        if (StringUtils.isNotBlank(localeStr)) {
            Locale locale = new Locale.Builder().setLanguageTag(localeStr).build();
            if (LocaleUtils.isAvailableLocale(locale)) {
                return locale;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Directory Layout                                                      //
    ///////////////////////////////////////////////////////////////////////////

    // standard dir names used by the app
    public static final String AUTOCOMPLETE_DIR_NAME = "autocomplete";
    public static final String CONFIG_DIR_NAME = "config";
    public static final String CACHE_DIR_NAME = "cache";
    public static final String LIB_DIR_NAME = "lib";
    public static final String PLUGINS_DIR_NAME = "plugins";
    public static final String USER_DIR_NAME = "user";

    /**
     * This is where all user data (both config and plugins) are stored. We use single
     * directory for all kinds of app data because not every OS has a notion of "config" and
     * "data" dirs. Overall user dir is supposed to look like this:
     * <pre>
     * ├── autocomplete
     * ├── cache
     * ├── config
     * │   ├── foo.cfg
     * │   └── bar.cfg
     * ├── plugins
     * │   └── plugin-module-name
     * │       ├── config
     * │       │   └── baz.cfg
     * │       └── lib
     * │           └── plugin.jar
     * </pre>
     */
    public static final Path USER_DIR = findUserDir();

    /** This is where all application configs are stored. */
    public static final Path CONFIG_DIR = USER_DIR.resolve(CONFIG_DIR_NAME);

    /** This where app searches for auto-completion / auto-suggestion data files. */
    public static final Path AUTOCOMPLETE_DIR = USER_DIR.resolve(AUTOCOMPLETE_DIR_NAME);

    /** This where app stores its caches. Temporary files IS NOT a cache. */
    public static final Path CACHE_DIR = USER_DIR.resolve(CACHE_DIR_NAME);

    /** This where app stores its logs. */
    public static final Path LOGS_DIR = USER_DIR;

    /** This where all plugins live. */
    public static final Path PLUGINS_DIR = USER_DIR.resolve(PLUGINS_DIR_NAME);

    /** See {@link #USER_DIR}. */
    public static Path findUserDir() {
        // env variable has top priority
        String userDirProp = getPropertyOrEnv("corf.user.dir", "CORF_USER_DIR");
        if (userDirProp != null && !userDirProp.isBlank()) { return Paths.get(userDirProp); }

        // first try to use conventional OS-dependent paths
        Path dataDir = null;
        if (SystemUtils.IS_OS_UNIX) {
            var xdgHome = OS.getXdgConfigDir();
            if (xdgHome == null && FileSystemUtils.dirExists(HOME_DIR.resolve(".config"))) {
                dataDir = HOME_DIR.resolve(".config");
            }
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            var addDataDir = OS.getLocalAppDataDir();
            if (addDataDir == null && FileSystemUtils.dirExists(HOME_DIR.resolve("AppData").resolve("Local"))) {
                dataDir = HOME_DIR.resolve("AppData").resolve("Local");
            }
        }

        // fallback to the home directory
        return Objects.requireNonNullElse(dataDir, HOME_DIR).resolve(APP_NAME);
    }

    /** See {@link #HOME_DIR}. */
    public static Path findHomeDir() {
        var prop = System.getProperty("user.home");
        return prop != null ? Paths.get(prop) : Paths.get("home"); // prevent NPE
    }

    /** See {@link #TEMP_DIR}. */
    public static Path findTempDir() {
        var prop = System.getProperty("java.io.tmpdir");
        return prop != null ? Paths.get(prop) : Paths.get("temp"); // prevent NPE
    }

    /** Returns the path where app stores the given plugin with all its data. */
    public static Path getPluginDir(Class<? extends Plugin> pluginClass) {
        return PLUGINS_DIR.resolve(pluginClass.getModule().getName());
    }

    /** Returns the path where app stores the given plugin config. */
    public static Path getPluginConfigDir(Class<? extends Plugin> pluginClass) {
        return getPluginDir(pluginClass).resolve(CONFIG_DIR_NAME);
    }

    /** Returns the path where app stores the given plugin JAR and its dependencies. */
    public static Path getPluginLibDir(Class<? extends Plugin> pluginClass) {
        return getPluginDir(pluginClass).resolve(LIB_DIR_NAME);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Desktop                                                               //
    ///////////////////////////////////////////////////////////////////////////

    /** Minimal recommended window size. */
    public static final Dimension MIN_WINDOW_SIZE = new Dimension(800, 600);

    /** Special value that means window is maximized. */
    public static final Dimension FULLSCREEN_SIZE = new Dimension(0, 0);

    private static final String FORMAT_TTF = "ttf";
    private static final String FORMAT_OTF = "otf";

    private static final ObjectProperty<Path> lastVisitedDir = new SimpleObjectProperty<>(Env.HOME_DIR);

    /** Returns the path to the last visited directory. */
    public static Path getLastVisitedDir() {
        return lastVisitedDir.get();
    }

    /** Sets the path to the last visited directory. */
    public static void setLastVisitedDir(@Nullable File file) {
        if (file != null && FileSystemUtils.fileExists(file.toPath())) {
            lastVisitedDir.set(FileSystemUtils.getParentPath(file));
            return;
        }

        if (file != null && FileSystemUtils.dirExists(file.toPath())) {
            lastVisitedDir.set(FileSystemUtils.getParentPath(file));
            return;
        }

        lastVisitedDir.set(Env.HOME_DIR);
    }

    /** Returns application interface font. */
    public static InputStream getInterfaceFont(FontWeight weight, FontPosture posture) {
        return Env.class.getResourceAsStream(Objects.requireNonNull(
                getFont("Inter", FORMAT_OTF, weight, posture)
        ));
    }

    /** Returns application monospace font. */
    public static InputStream getMonospaceFont(FontWeight weight) {
        return Env.class.getResourceAsStream(Objects.requireNonNull(
                getFont("FiraMono", FORMAT_TTF, weight, FontPosture.REGULAR)
        ));
    }

    /** Returns document font for printing. */
    public static InputStream getDocumentFont(FontWeight weight, FontPosture posture) {
        return Env.class.getResourceAsStream(Objects.requireNonNull(
                getFont("Roboto", FORMAT_TTF, weight, posture)
        ));
    }

    private static @Nullable String getFont(String family, String format, FontWeight weight, FontPosture posture) {
        return switch (weight) {
            case BOLD, EXTRA_BOLD, BLACK -> getFontPath(family, "Bold", format);
            case MEDIUM, SEMI_BOLD -> getFontPath(family, "Medium", format);
            default -> posture != FontPosture.ITALIC
                    ? getFontPath(family, "Regular", format)
                    : getFontPath(family, "Italic", format);
        };
    }

    private static String getFontPath(String family, String fontType, String fontFormat) {
        String subPath = family + "/" + family + "-" + fontType + "." + fontFormat;
        return BASE_MODULE.concat("assets/fonts/" + subPath).toString();
    }
}
