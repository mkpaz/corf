package telekit.base.domain.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.PasswordAuthentication;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UsernamePasswordCredentials extends PasswordCredentials {

    private final String username;

    @JsonCreator
    public UsernamePasswordCredentials(@JsonProperty("name") String name,
                                       @JsonProperty("username") String username,
                                       @JsonProperty("password") char[] password) {
        super(name, password);
        this.username = Objects.requireNonNull(username);

        if (isBlank(username)) { throw new IllegalArgumentException("Username can't be empty."); }
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
        return of(username, isNotBlank(password) ? password.trim().toCharArray() : new char[]{});
    }

    public static UsernamePasswordCredentials of(String username, char[] password) {
        return new UsernamePasswordCredentials("", username.trim(), password);
    }
}
