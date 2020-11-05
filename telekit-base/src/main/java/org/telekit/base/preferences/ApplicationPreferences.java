package org.telekit.base.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.Env;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.telekit.base.Env.DATA_DIR;
import static org.telekit.base.i18n.BaseMessageKeys.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE;
import static org.telekit.base.i18n.BaseMessageKeys.MGG_UNABLE_TO_SAVE_DATA_TO_FILE;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationPreferences {

    @Deprecated
    public static final Path CONFIG_PATH_OLD = DATA_DIR.resolve("preferences.xml");
    public static final Path CONFIG_PATH = DATA_DIR.resolve("preferences.yaml");

    private Language language = Language.EN;
    private boolean systemTray = false;
    private Proxy proxy;
    private Security security = new Security();

    @JacksonXmlElementWrapper(localName = "disabledPlugins")
    @JacksonXmlProperty(localName = "item")
    private Set<String> disabledPlugins = new HashSet<>();

    // indicates that preferences changes has been made
    private boolean dirty = false;

    public ApplicationPreferences() {}

    public @NotNull Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language != null ? language : Language.EN;
    }

    public boolean isSystemTray() {
        return systemTray;
    }

    public void setSystemTray(boolean systemTray) {
        this.systemTray = systemTray;
    }

    public @Nullable Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public @NotNull Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security != null ? security : new Security();
    }

    public @NotNull Set<String> getDisabledPlugins() {
        return disabledPlugins;
    }

    public void setDisabledPlugins(Set<String> disabledPlugins) {
        this.disabledPlugins = disabledPlugins != null ? disabledPlugins : new HashSet<>();
    }

    @JsonIgnore
    public @NotNull Locale getLocale() {
        // env variable is only needed to simplify app testing
        return Env.LOCALE != null ? Env.LOCALE : language.getLocale();
    }

    @JsonIgnore
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        this.dirty = true;
    }

    public void resetDirty() {
        this.dirty = false;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "language=" + language +
                ", systemTray=" + systemTray +
                ", proxy=" + proxy +
                ", security=" + security +
                ", disabledPlugins=" + disabledPlugins +
                '}';
    }

    public static ApplicationPreferences load(YAMLMapper mapper, Path path) {
        try {
            return mapper.readValue(CONFIG_PATH.toFile(), ApplicationPreferences.class);
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public static void save(ApplicationPreferences preferences, YAMLMapper mapper, Path path) {
        try {
            mapper.writeValue(CONFIG_PATH.toFile(), preferences);
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }
}
