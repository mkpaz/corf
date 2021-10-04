package telekit.base.preferences.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.jetbrains.annotations.Nullable;
import telekit.base.preferences.Proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@JsonRootName(value = "proxy")
public class ProxyPreferences {

    private String activeProfile;
    private List<Proxy> profiles = new ArrayList<>();

    public ProxyPreferences() {}

    @JsonProperty("active")
    public String getActiveProfile() {
        return defaultIfBlank(activeProfile, Proxy.DISABLED);
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = defaultIfBlank(activeProfile, Proxy.DISABLED);
    }

    public List<Proxy> getProfiles() {
        return defaultIfNull(profiles, new ArrayList<>());
    }

    public void setProfiles(List<Proxy> profiles) {
        this.profiles = defaultIfNull(profiles, new ArrayList<>());
    }

    @JsonIgnore
    public @Nullable Proxy getActiveProxy() {
        return profiles.stream()
                .filter(proxy -> activeProfile.equals(proxy.getId()))
                .findFirst()
                .orElse(null);
    }

    public void addOrUpdateProxy(Proxy proxy) {
        int pos = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (Objects.equals(profiles.get(i).getId(), proxy.getId())) {
                pos = i;
                break;
            }
        }

        if (pos < 0) {
            profiles.add(proxy);
        } else {
            profiles.set(pos, proxy);
        }
    }

    @Override
    public String toString() {
        return "ProxyPreferences{" +
                "activeProfile='" + activeProfile + '\'' +
                ", profiles=" + profiles +
                '}';
    }
}
