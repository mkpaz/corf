package org.telekit.base.domain;

public class AuthPrincipal {

    private final String username;
    private final String password;

    public AuthPrincipal(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "AuthPrincipal{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
