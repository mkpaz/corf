package org.telekit.base.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.domain.TelekitException;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.telekit.base.Settings.DATA_DIR;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferences {

    public static final Path CONFIG_PATH = DATA_DIR.resolve("preferences.xml");

    @JacksonXmlElementWrapper(localName = "disabledPlugins")
    @JacksonXmlProperty(localName = "item")
    private Set<String> disabledPlugins = new HashSet<>();

    public UserPreferences() {}

    @NotNull
    public Set<String> getDisabledPlugins() {
        return disabledPlugins;
    }

    public void setDisabledPlugins(Set<String> disabledPlugins) {
        this.disabledPlugins = disabledPlugins != null ? disabledPlugins : new HashSet<>();
    }

    @Override
    public String toString() {
        return "Preferences{" +
                "disabledPlugins=" + disabledPlugins +
                '}';
    }

    public static UserPreferences load(XmlMapper mapper, Path path) {
        try {
            return mapper.readValue(CONFIG_PATH.toFile(), UserPreferences.class);
        } catch (Exception e) {
            throw new TelekitException("Unable to parse preferences file", e);
        }
    }

    public static void store(UserPreferences preferences, XmlMapper mapper, Path path) {
        try {
            mapper.writeValue(CONFIG_PATH.toFile(), preferences);
        } catch (Exception e) {
            throw new TelekitException("Unable to save preferences", e);
        }
    }
}
