package corf.desktop.startup;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import corf.base.Env;
import corf.base.i18n.I18n;
import corf.base.i18n.M;
import corf.base.io.FileSystemUtils;
import corf.base.io.JacksonMappers;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.desktop.i18n.DM;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PreferencesConfig implements Config {

    private static final String APP_PROPS_FILE_NAME = "application.properties";

    private final YAMLMapper yamlMapper;
    private ApplicationPreferences preferences;

    public PreferencesConfig() {
        this.yamlMapper = JacksonMappers.createYamlMapper();
        init();
    }

    private void init() {
        loadApplicationProperties();
        createUserResources();

        // set unlimited crypto policy
        java.security.Security.setProperty("crypto.policy", "unlimited");

        // load (or create) application preferences
        var firstRun = false;
        if (Files.exists(ApplicationPreferences.CONFIG_PATH)) {
            // application can't work without preferences, don't make attempts to recover
            preferences = ApplicationPreferences.load(yamlMapper);
        } else {
            preferences = new ApplicationPreferences();
            firstRun = true;
        }

        // first run is also a time to detect some defaults
        if (firstRun) {
            // pick app language from system if supported
            // Arrays.stream(Language.values())
            //        .filter(lang -> lang.getLocale().getLanguage().equalsIgnoreCase(Locale.getDefault().getLanguage()))
            //        .findFirst()
            //        .ifPresent(language -> preferences.setLanguage(language));

            ApplicationPreferences.save(preferences, yamlMapper);
        }

        loadI18nResources();
    }

    private void loadApplicationProperties() {
        try {
            var properties = new Properties();
            properties.load(new InputStreamReader(getResourceAsStream(APP_PROPS_FILE_NAME), UTF_8));

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                System.setProperty(
                        String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createUserResources() {
        // Env.APP_DIR may not exist, so use 'mkdir -p'
        FileSystemUtils.createDirTree(Env.USER_DIR);

        FileSystemUtils.createDir(Env.AUTOCOMPLETE_DIR);
        FileSystemUtils.createDir(Env.CONFIG_DIR);
        FileSystemUtils.createDir(Env.CACHE_DIR);
        FileSystemUtils.createDir(Env.PLUGINS_DIR);
    }

    private void loadI18nResources() {
        // set default locale and load resource bundles
        // after that any component can just call Locale.getDefault()
        Locale.setDefault(preferences.getLocale());
        I18n.getInstance().register(M.getLoader());
        I18n.getInstance().register(DM.getLoader());
        I18n.getInstance().reload();
    }

    public ApplicationPreferences getPreferences() {
        return preferences;
    }
}
