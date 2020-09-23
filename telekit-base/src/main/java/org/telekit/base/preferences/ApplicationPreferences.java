package org.telekit.base.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.Environment;
import org.telekit.base.domain.TelekitException;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.telekit.base.Environment.DATA_DIR;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationPreferences {

    public static final Path CONFIG_PATH = DATA_DIR.resolve("preferences.xml");

    private Language language = Language.EN;
    private boolean systemTray = false;
    private Proxy proxy;

    @JacksonXmlElementWrapper(localName = "disabledPlugins")
    @JacksonXmlProperty(localName = "item")
    private Set<String> disabledPlugins = new HashSet<>();

    public ApplicationPreferences() {}

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isSystemTray() {
        return systemTray;
    }

    public void setSystemTray(boolean systemTray) {
        this.systemTray = systemTray;
    }

    public @Nullable Proxy getProxy() {
        return proxy != null && proxy.isValid() ? proxy : null;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
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
        return Environment.LOCALE != null ? Environment.LOCALE : language.getLocale();
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "language=" + language +
                ", systemTray=" + systemTray +
                ", proxy=" + proxy +
                ", disabledPlugins=" + disabledPlugins +
                '}';
    }

    public static ApplicationPreferences load(XmlMapper mapper, Path path) {
        try {
            return mapper.readValue(CONFIG_PATH.toFile(), ApplicationPreferences.class);
        } catch (Exception e) {
            throw new TelekitException("Unable to parse preferences file", e);
        }
    }

    public static void store(ApplicationPreferences preferences, XmlMapper mapper, Path path) {
        try {
            mapper.writeValue(CONFIG_PATH.toFile(), preferences);
        } catch (Exception e) {
            throw new TelekitException("Unable to save preferences", e);
        }
    }
}
