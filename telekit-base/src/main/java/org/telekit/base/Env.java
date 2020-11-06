package org.telekit.base;

import org.apache.commons.lang3.LocaleUtils;
import org.telekit.base.plugin.Plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.service.Encryptor.Algorithm;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.base.util.CommonUtils.getPropertyOrEnv;
import static org.telekit.base.util.FileUtils.ensureNotNull;

public final class Env {

    public static final String APP_NAME = "Telekit";
    public static final Algorithm DEFAULT_ENCRYPTION_ALG = Algorithm.AES_GCM;

    // TODO: [0.9] rename APP_DIR to ROOT_DIR and backup env value  with 'user.home'
    public static final Path APP_DIR = Paths.get(
            getPropertyOrEnv("telekit.app.dir", "TELEKIT_APP_DIR")
    );
    public static final Path DATA_DIR = ensureNotNull(
            getPropertyOrEnv("telekit.data.dir", "TELEKIT_DATA_DIR"), APP_DIR.resolve("data")
    );
    public static final Path PLUGINS_DIR = ensureNotNull(
            getPropertyOrEnv("telekit.plugins.dir", "TELEKIT_PLUGINS_DIR"), APP_DIR.resolve("plugins")
    );
    public static final Path DOCS_DIR = APP_DIR.resolve("docs");
    public static final Path HOME_DIR = Paths.get(System.getProperty("user.home"));
    public static final Path OS_TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    public static final Path APP_TEMP_DIR = OS_TEMP_DIR.resolve("telekit");

    public static final String FORCED_WINDOWS_SIZE =
            getPropertyOrEnv("telekit.window.size", "TELEKIT_WINDOW_SIZE");

    public static Path getPluginDataDir(Class<? extends Plugin> pluginClass) {
        return DATA_DIR.resolve(className(pluginClass));
    }

    public static Path getPluginDocsDir(Class<? extends Plugin> pluginClass) {
        return getPluginDataDir(pluginClass).resolve(Plugin.PLUGIN_DOCS_DIR_NAME);
    }

    // TODO: Find a better way to provide app version. This way in can be manipulated at runtime.
    public static String getAppVersion() {
        return System.getProperty("application.version");
    }

    // TODO: [0.9] inspect all locale sources and refactor its extraction to more readable fashion
    public static final Locale LOCALE = getLocaleFromEnv();

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
