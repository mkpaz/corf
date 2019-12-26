package corf.base.preferences.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.exception.AppException;
import corf.base.security.UsernamePasswordCredentials;
import corf.base.i18n.M;
import corf.base.net.BaseConnectionParams;
import corf.base.net.ConnectionParams;
import corf.base.net.Scheme;
import corf.base.preferences.Proxy;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static corf.base.i18n.I18n.t;

public class ManualProxy implements Proxy {

    public static final String ID = "manual";
    public static final Set<Scheme> SUPPORTED_SCHEMES = Set.of(Scheme.HTTP, Scheme.HTTPS);

    private final URI uri;
    private final @Nullable UsernamePasswordCredentials credentials;
    private final List<String> exceptions;

    @JsonCreator
    public ManualProxy(@JsonProperty("uri") URI uri,
                       @JsonProperty("credentials") @Nullable UsernamePasswordCredentials credentials,
                       @JsonProperty("exceptions") @Nullable List<String> exceptions) {
        this.uri = Objects.requireNonNull(uri, "uri");
        this.credentials = credentials;
        this.exceptions = ListUtils.defaultIfNull(exceptions, Collections.emptyList());

        Scheme scheme = getScheme();
        if (scheme == null || !SUPPORTED_SCHEMES.contains(scheme)) {
            throw new AppException(t(M.MSG_INVALID_PARAM, "scheme: " + scheme));
        }

        if (StringUtils.isBlank(getHost())) {
            throw new AppException(t(M.MSG_INVALID_PARAM, "host: " + getHost()));
        }

        if (getPort() < 0) {
            throw new AppException(t(M.MSG_INVALID_PARAM, "port: " + getPort()));
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    public URI getUri() {
        return uri;
    }

    public @Nullable UsernamePasswordCredentials getCredentials() {
        return credentials;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    @Override
    public @Nullable ConnectionParams getConnectionParams(String ipOrHostname) {
        boolean passAsException = exceptions.stream().anyMatch(e -> Proxy.match(e, ipOrHostname));
        if (passAsException) { return null; }

        return new BaseConnectionParams(getScheme(), getHost(), getPort(), credentials);
    }

    @JsonIgnore
    public Scheme getScheme() {
        return Objects.requireNonNull(Scheme.fromString(uri.getScheme()), "Unknown URI scheme.");
    }

    @JsonIgnore
    public String getHost() {
        return uri.getHost();
    }

    @JsonIgnore
    public int getPort() {
        return uri.getPort() > 0 ? uri.getPort() : getScheme().getWellKnownPort();
    }

    @Override
    public String toString() {
        return "ManualProxy{" +
                "uri=" + uri +
                ", credentials=" + credentials +
                ", exceptions=" + exceptions +
                '}';
    }
}
