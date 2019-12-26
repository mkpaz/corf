package corf.base.preferences.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.preferences.Proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName(value = "proxy")
public class ProxyPreferences {

    public static final ProxyPreferences NO_PROXY = new ProxyPreferences();

    private String activeProfile = Proxy.OFF;
    private List<Proxy> profiles = new ArrayList<>();

    public ProxyPreferences() { }

    @JsonProperty("active")
    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(@Nullable String activeProfile) {
        this.activeProfile = StringUtils.defaultIfBlank(activeProfile, Proxy.OFF);
    }

    public List<Proxy> getProfiles() {
        return profiles;
    }

    public void setProfiles(@Nullable List<Proxy> profiles) {
        this.profiles = Objects.requireNonNullElse(profiles, new ArrayList<>());
    }

    @JsonIgnore
    public @Nullable Proxy getActiveProxy() {
        return profiles.stream()
                .filter(proxy -> Objects.equals(activeProfile, proxy.getId()))
                .findFirst()
                .orElse(null);
    }

    public void addOrUpdateProxy(Proxy proxy) {
        Objects.requireNonNull(proxy, "proxy");

        int pos = ListUtils.indexOf(profiles, profile -> Objects.equals(profile.getId(), proxy.getId()));
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
