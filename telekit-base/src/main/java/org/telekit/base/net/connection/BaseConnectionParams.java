package org.telekit.base.net.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.domain.security.Credentials;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.base.i18n.BaseMessages.MSG_INVALID_PARAM;
import static org.telekit.base.i18n.I18n.t;

public class BaseConnectionParams implements ConnectionParams {

    protected final Scheme scheme;
    protected final String host;
    protected final int port;
    protected final Credentials credentials;

    @JsonCreator
    public BaseConnectionParams(@JsonProperty("scheme") Scheme scheme,
                                @JsonProperty("host") String host,
                                @JsonProperty("port") int port) {
        this(scheme, host, port, null);
    }

    public BaseConnectionParams(@JsonProperty("scheme") Scheme scheme,
                                @JsonProperty("host") String host,
                                @JsonProperty("port") int port,
                                @JsonProperty("credentials") Credentials credentials) {
        this.scheme = Objects.requireNonNull(scheme);

        if (isBlank(host)) { throw new TelekitException(t(MSG_INVALID_PARAM, host)); }
        this.host = host;

        if (port < 0) { throw new TelekitException(t(MSG_INVALID_PARAM, port)); }
        this.port = port;

        this.credentials = credentials;
    }

    @Override
    public Scheme getScheme() { return scheme; }

    @Override
    public String getHost() { return host; }

    @Override
    public int getPort() { return port; }

    @Override
    public @Nullable Credentials getCredentials() { return credentials; }

    @Override
    public String toString() {
        return "BaseConnectionParams{" +
                "scheme=" + scheme +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", credential=" + credentials +
                '}';
    }
}
