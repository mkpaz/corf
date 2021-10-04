package telekit.base.net.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import telekit.base.domain.exception.TelekitException;
import telekit.base.domain.security.Credentials;

import java.net.URI;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static telekit.base.i18n.BaseMessages.MSG_INVALID_PARAM;
import static telekit.base.i18n.I18n.t;

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
                                @JsonProperty("credentials") @Nullable Credentials credentials) {
        this.scheme = Objects.requireNonNull(scheme);

        if (isBlank(host)) { throw new TelekitException(t(MSG_INVALID_PARAM, host)); }
        this.host = host;

        if (port < -1) { throw new TelekitException(t(MSG_INVALID_PARAM, port)); }
        this.port = port != 0 ? port : -1;

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

    ///////////////////////////////////////////////////////////////////////////

    public static BaseConnectionParams fromUrl(URI uri) {
        return fromUrl(uri, null);
    }

    public static BaseConnectionParams fromUrl(URI uri, @Nullable Credentials credentials) {
        Objects.requireNonNull(uri);

        Scheme scheme = Scheme.fromString(uri.getScheme());
        if (scheme == null) {
            throw new IllegalArgumentException("Unknown URI scheme: '" + scheme + "'");
        }

        int port = uri.getPort() > 0 ? uri.getPort() : scheme.getWellKnownPort();

        return new BaseConnectionParams(scheme, uri.getHost(), port, credentials);
    }
}
