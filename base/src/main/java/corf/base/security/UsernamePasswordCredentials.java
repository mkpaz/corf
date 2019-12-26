package corf.base.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.net.PasswordAuthentication;
import java.util.Objects;

public class UsernamePasswordCredentials extends PasswordCredentials {

    private final String username;

    @JsonCreator
    public UsernamePasswordCredentials(@JsonProperty("name") String name,
                                       @JsonProperty("username") String username,
                                       @JsonProperty("password") char[] password) {
        super(name, password);
        this.username = Objects.requireNonNull(username, "username");

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public PasswordAuthentication toPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }

    @Override
    public String toString() {
        return "UsernamePasswordCredentials{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password=********" +
                '}';
    }

    public static UsernamePasswordCredentials of(String username, String password) {
        var psw = StringUtils.isNotBlank(password) ? password.trim().toCharArray() : new char[] { };
        return new UsernamePasswordCredentials("", username, psw);
    }

    public static UsernamePasswordCredentials of(String username, char[] password) {
        return new UsernamePasswordCredentials("", username, password);
    }
}
