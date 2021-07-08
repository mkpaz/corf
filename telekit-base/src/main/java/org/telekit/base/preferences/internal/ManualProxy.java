package org.telekit.base.preferences.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.domain.security.UsernamePasswordCredentials;
import org.telekit.base.net.connection.BaseConnectionParams;
import org.telekit.base.net.connection.ConnectionParams;
import org.telekit.base.net.connection.Scheme;
import org.telekit.base.preferences.Proxy;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.apache.commons.collections4.ListUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.base.i18n.BaseMessages.MSG_INVALID_PARAM;
import static org.telekit.base.i18n.I18n.t;

public class ManualProxy implements Proxy {

    public static final String ID = "manual";
    public static final Set<Scheme> SUPPORTED_SCHEMES = Set.of(Scheme.HTTP);

    private final URI uri;
    private final UsernamePasswordCredentials credentials;
    private final List<String> exceptions;

    @JsonCreator
    public ManualProxy(@JsonProperty("uri") URI uri,
                       @JsonProperty("credentials") UsernamePasswordCredentials credentials,
                       @JsonProperty("exceptions") List<String> exceptions) {
        this.uri = uri;
        this.credentials = credentials;
        this.exceptions = defaultIfNull(exceptions, Collections.emptyList());

        Scheme scheme = getScheme();
        if (scheme == null || !SUPPORTED_SCHEMES.contains(scheme)) {
            throw new TelekitException(t(MSG_INVALID_PARAM, "scheme: " + scheme));
        }

        if (isBlank(getHost())) {
            throw new TelekitException(t(MSG_INVALID_PARAM, "host: " + getHost()));
        }

        if (getPort() < 0) {
            throw new TelekitException(t(MSG_INVALID_PARAM, "port: " + getPort()));
        }
    }

    @Override
    public String getId() { return ID; }

    public URI getUri() { return uri; }

    public UsernamePasswordCredentials getCredentials() { return credentials; }

    public List<String> getExceptions() { return exceptions; }

    @Override
    public @Nullable ConnectionParams getConnectionParams(String ipOrHostname) {
        boolean shouldNotProxy = exceptions.stream().anyMatch(e -> Proxy.match(e, ipOrHostname));
        if (shouldNotProxy) { return null; }

        return new BaseConnectionParams(getScheme(), getHost(), getPort(), credentials);
    }

    @JsonIgnore
    public Scheme getScheme() { return Scheme.fromString(uri.getScheme()); }

    @JsonIgnore
    public String getHost() { return uri.getHost(); }

    @JsonIgnore
    public int getPort() { return uri.getPort() > 0 ? uri.getPort() : getScheme().getWellKnownPort(); }

    @Override
    public String toString() {
        return "ManualProxy{" +
                "uri=" + uri +
                ", credentials=" + credentials +
                ", exceptions=" + exceptions +
                '}';
    }
}
