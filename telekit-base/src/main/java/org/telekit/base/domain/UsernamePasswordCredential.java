package org.telekit.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.PasswordAuthentication;
import java.util.Objects;

public class UsernamePasswordCredential extends Credential {

    private final String username;
    private final char[] password;

    public UsernamePasswordCredential(String username, char[] password) {
        this(username, username, password);
    }

    public UsernamePasswordCredential(String name, String username, char[] password) {
        super(name);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        if (password.length == 0) { throw new IllegalArgumentException("Password can't be empty."); }
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    @JsonIgnore
    public String getPasswordAsString() {
        return new String(password);
    }

    @JsonIgnore
    public PasswordAuthentication toPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }

    @Override
    public String toString() {
        return "UsernamePasswordCredential{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='********'" +
                "} " + super.toString();
    }

    public static UsernamePasswordCredential of(String username, String password) {
        return new UsernamePasswordCredential(username.trim(), password.trim().toCharArray());
    }
}
