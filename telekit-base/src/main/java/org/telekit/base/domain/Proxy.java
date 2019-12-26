package org.telekit.base.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.net.Scheme;

import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Proxy {

    public static final Proxy NO_PROXY = Proxy.of(URI.create("system://127.0.0.1"));

    private final URI uri;
    private String username;
    private char[] password;

    @JsonCreator
    public Proxy(@JsonProperty("uri") URI uri) {
        this.uri = Objects.requireNonNull(uri);
    }

    public Proxy(Proxy proxy) {
        this.uri = proxy.getUri();
        this.username = proxy.getUsername();
        this.password = proxy.getPassword();
    }

    public URI getUri() {
        return uri;
    }

    public @Nullable String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    @JsonIgnore
    public @Nullable String getPasswordAsString() {
        return password != null && password.length > 0 ? new String(password) : null;
    }

    @JsonIgnore
    public @Nullable UsernamePasswordCredential credential() {
        return hasUsername() && hasPassword() ? new UsernamePasswordCredential(username, password) : null;
    }

    @JsonIgnore
    public @Nullable PasswordAuthentication passwordAuthentication() {
        return hasUsername() && hasPassword() ? new PasswordAuthentication(username, password) : null;
    }

    @JsonIgnore
    public boolean isValid() {
        return Scheme.collection(Scheme.HTTP, Scheme.HTTPS).contains(uri.getScheme()) && !NO_PROXY.equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Proxy proxy = (Proxy) o;
        return uri.equals(proxy.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "uri='" + uri + '\'' +
                ", username='" + username + '\'' +
                ", password='********'" +
                '}';
    }

    private boolean hasUsername() {
        return isNotBlank(username);
    }

    private boolean hasPassword() {
        return password != null && password.length > 0;
    }

    public static Proxy of(URI uri) {
        return of(uri, parseUserInfo(uri.getUserInfo()));
    }

    public static Proxy of(URI uri, @Nullable UsernamePasswordCredential credential) {
        Proxy proxy = new Proxy(uri);
        if (credential != null) {
            proxy.setUsername(credential.getUsername());
            proxy.setPassword(credential.getPassword());
        }
        return proxy;
    }

    private static @Nullable UsernamePasswordCredential parseUserInfo(String userInfo) {
        if (userInfo == null) { return null; }
        String[] userPass = userInfo.split(":");
        return userPass.length == 2 ? UsernamePasswordCredential.of(userPass[0], userPass[1]) : null;
    }
}
