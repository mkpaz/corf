package telekit.base.domain.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public class PasswordCredentials extends Credentials {

    protected final char[] password;

    @JsonCreator
    public PasswordCredentials(@JsonProperty("name") String name,
                               @JsonProperty("password") char[] password) {
        super(name);

        this.password = password != null ? password : new char[]{};
    }

    // always clone mutable passwords, because some tools for security reasons
    // clean-up password array after it has been used
    public char[] getPassword() { return password.clone(); }

    @JsonIgnore
    public @Nullable String getPasswordAsString() {
        return password.length > 0 ? new String(password) : null;
    }

    @Override
    public String toString() {
        return "PasswordCredentials{" +
                "name='" + name + '\'' +
                ", password=********" +
                '}';
    }
}
