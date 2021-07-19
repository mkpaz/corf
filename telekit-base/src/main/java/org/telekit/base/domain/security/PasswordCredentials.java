package org.telekit.base.domain.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PasswordCredentials extends Credentials {

    protected final char[] password;

    @JsonCreator
    public PasswordCredentials(@JsonProperty("name") String name,
                               @JsonProperty("password") char[] password) {
        super(name);
        this.password = password;

        if (password.length == 0) { throw new IllegalArgumentException("Password can't be empty."); }
    }

    // always clone mutable passwords, because some tools for security reasons
    // clean-up password array after it has been used
    public char[] getPassword() { return password.clone(); }

    @JsonIgnore
    public String getPasswordAsString() { return new String(password); }

    @Override
    public String toString() {
        return "PasswordCredentials{" +
                "name='" + name + '\'' +
                ", password=********" +
                '}';
    }
}
