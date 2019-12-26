package corf.base.preferences.internal;

import atlantafx.base.theme.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.exception.AppException;
import corf.base.i18n.M;
import corf.base.io.FileSystemUtils;
import corf.base.preferences.SystemPreferences;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.prefs.Preferences;

import static corf.base.i18n.I18n.t;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationPreferences {

    public static final Path CONFIG_PATH = Env.CONFIG_DIR.resolve("preferences.yaml");
    public static final Theme DEFAULT_THEME = new PrimerLight();
    public static final List<Theme> THEMES = List.of(
            DEFAULT_THEME, new PrimerDark(), new NordLight(), new NordDark()
    );
    public static final Preferences USER_ROOT = Preferences.userRoot().node(Env.APP_NAME);

    private final Language language = Language.EN;
    private String theme = DEFAULT_THEME.getName();
    private Set<String> disabledPlugins = new HashSet<>();
    private ProxyPreferences proxyPreferences = ProxyPreferences.NO_PROXY;

    // java.util.Preferences for storing host-related config
    private final SystemPreferences systemPreferences = new SystemPreferences(USER_ROOT);

    /** Default constructor */
    public ApplicationPreferences() { }

    public Language getLanguage() {
        return language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(@Nullable String theme) {
        this.theme = Objects.requireNonNull(theme, DEFAULT_THEME.getName());
    }

    @JsonProperty("proxy")
    public ProxyPreferences getProxyPreferences() {
        return proxyPreferences;
    }

    public void setProxyPreferences(@Nullable ProxyPreferences proxyPreferences) {
        this.proxyPreferences = Objects.requireNonNullElse(proxyPreferences, ProxyPreferences.NO_PROXY);
    }

    public Set<String> getDisabledPlugins() {
        return disabledPlugins;
    }

    public void setDisabledPlugins(@Nullable Set<String> disabledPlugins) {
        this.disabledPlugins = Objects.requireNonNullElse(disabledPlugins, new HashSet<>());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Extended API                                                          //
    ///////////////////////////////////////////////////////////////////////////

    @JsonIgnore
    public Theme getStyleTheme() {
        var existingTheme = findThemeByName(theme);
        return existingTheme != null ? existingTheme : DEFAULT_THEME;
    }

    public void setStyleTheme(@Nullable Theme theme) {
        Objects.requireNonNull(theme, "theme");

        var existingTheme = findThemeByName(theme.getName());
        if (existingTheme == null) {
            throw new IllegalArgumentException("Unknown theme: " + theme.getName() + ".");
        }

        this.theme = existingTheme.getName();
    }

    @JsonIgnore
    private @Nullable Theme findThemeByName(String name) {
        return THEMES.stream()
                .filter(theme -> Objects.equals(theme.getName(), name))
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public Locale getLocale() {
        // env variable has priority, but is only needed to simplify app testing,
        // this way can change app language without messing with UI
        return ObjectUtils.defaultIfNull(Env.LOCALE, language.getLocale());
    }

    @JsonIgnore
    public SystemPreferences getSystemPreferences() {
        return systemPreferences;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Save / Load                                                           //
    ///////////////////////////////////////////////////////////////////////////

    public static ApplicationPreferences load(YAMLMapper mapper) {
        return load(mapper, CONFIG_PATH);
    }

    public static ApplicationPreferences load(YAMLMapper mapper, Path path) {
        try {
            return mapper.readValue(path.toFile(), ApplicationPreferences.class);
        } catch (Exception e) {
            throw new AppException(t(M.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public static void save(ApplicationPreferences preferences, YAMLMapper mapper) {
        save(preferences, mapper, CONFIG_PATH);
    }

    public static void save(ApplicationPreferences preferences, YAMLMapper mapper, Path path) {
        Path backup = FileSystemUtils.backupFile(path);

        try {
            mapper.writeValue(path.toFile(), preferences);
        } catch (Exception e) {
            if (backup != null) {
                FileSystemUtils.copyFile(backup, path, StandardCopyOption.REPLACE_EXISTING);
            }
            throw new AppException(t(M.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        } finally {
            if (backup != null) {
                try {
                    FileSystemUtils.deleteFile(backup);
                } catch (Throwable ignored) { /* ignore as tmp file isn't important */ }
            }
        }
    }
}
