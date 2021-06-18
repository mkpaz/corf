package org.telekit.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TokenCredential extends Credential {

    private final char[] token;

    public TokenCredential(String name, char[] token) {
        super(name);
        this.token = Objects.requireNonNull(token);
        if (token.length == 0) { throw new IllegalArgumentException("Token can't be empty."); }
    }

    public char[] getToken() {
        return token;
    }

    @JsonIgnore
    public @NotNull String getTokenAsString() {
        return new String(token);
    }

    @Override
    public String toString() {
        return "TokenCredential{" +
                "name='" + name + '\'' +
                ", token='********'" +
                "} " + super.toString();
    }

    public static TokenCredential of(@NotNull String name, @NotNull String token) {
        return new TokenCredential(name.trim(), token.trim().toCharArray());
    }
}
