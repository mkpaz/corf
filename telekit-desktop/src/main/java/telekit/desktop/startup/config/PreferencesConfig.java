package telekit.desktop.startup.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import telekit.base.i18n.BaseMessages;
import telekit.base.i18n.I18n;
import telekit.base.preferences.Theme;
import telekit.base.preferences.internal.ApplicationPreferences;
import telekit.base.preferences.internal.Language;
import telekit.base.util.Mappers;
import telekit.controls.i18n.ControlsMessages;
import telekit.controls.theme.DefaultTheme;
import telekit.desktop.i18n.DesktopMessages;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static telekit.base.Env.*;
import static telekit.base.util.FileSystemUtils.createDir;

public final class PreferencesConfig implements Config {

    private static final String APP_PROPS_FILE_NAME = "application.properties";
    private static final Theme DEFAULT_THEME = new DefaultTheme();

    private final YAMLMapper yamlMapper;
    private ApplicationPreferences preferences;

    public PreferencesConfig() {
        this.yamlMapper = Mappers.createYamlMapper();
        initialize();
    }

    private void initialize() {
        loadApplicationProperties();
        createUserResources();

        preferences = loadApplicationPreferences();
        preferences.setTheme(DEFAULT_THEME);

        // first run (or recover after file was deleted), good time to detect some defaults
        if (preferences.isDirty()) {
            // pick app language from system if supported
            Arrays.stream(Language.values())
                    .filter(lang -> lang.getLocale().getLanguage().equalsIgnoreCase(Locale.getDefault().getLanguage()))
                    .findFirst()
                    .ifPresent(language -> preferences.setLanguage(language));
        }

        loadI18nResources();
    }

    private void loadApplicationProperties() {
        try {
            InputStreamReader reader = new InputStreamReader(getResourceAsStream(APP_PROPS_FILE_NAME), UTF_8);

            Properties properties = new Properties();
            properties.load(reader);

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
        createDir(DATA_DIR);
        createDir(AUTOCOMPLETE_DIR);
        createDir(CONFIG_DIR);
        createDir(CACHE_DIR);
        createDir(PLUGINS_DIR);
    }

    private ApplicationPreferences loadApplicationPreferences() {
        if (Files.exists(ApplicationPreferences.CONFIG_PATH)) {
            // application can't work without preferences, don't make attempts to recover
            return ApplicationPreferences.load(yamlMapper);
        } else {
            ApplicationPreferences preferences = new ApplicationPreferences();
            preferences.setDirty();
            return preferences;
        }
    }

    private void loadI18nResources() {
        // set default locale and load resource bundles
        // after that any component can just call Locale.getDefault()
        Locale.setDefault(preferences.getLocale());
        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().register(ControlsMessages.getLoader());
        I18n.getInstance().register(DesktopMessages.getLoader());
        I18n.getInstance().reload();
    }

    public ApplicationPreferences getPreferences() {
        return preferences;
    }

    public void savePreferences() {
        if (preferences.isDirty()) {
            ApplicationPreferences.save(preferences, yamlMapper);
            preferences.resetDirty();
        }
    }
}
