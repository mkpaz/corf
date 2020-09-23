package org.telekit.base.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.domain.AuthPrincipal;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Proxy {

    private String url;
    private String username;
    private String password;

    public Proxy() {}

    public Proxy(Proxy proxy) {
        this.url = proxy.getUrl();
        this.username = proxy.getUsername();
        this.password = proxy.getPassword();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public @Nullable AuthPrincipal getPrincipal() {
        return isNotBlank(username) && isNotBlank(password) ? new AuthPrincipal(username, password) : null;
    }

    @JsonIgnore
    public boolean isValid() {
        // TODO: add decent url validator
        return !isBlank(url);
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
