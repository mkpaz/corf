package corf.base.net;

import org.jetbrains.annotations.Nullable;
import corf.base.security.Credentials;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public interface ConnectionParams {

    Scheme getScheme();

    String getHost();

    int getPort();

    @Nullable Credentials getCredentials();

    default InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    /**
     * Converts object to the corresponding URI representation.
     * This method <b>does not</b> store credentials in authority part of the URI.
     */
    default URI toUri() {
        try {
            return new URI(getScheme().toString().toLowerCase(), null, getHost(), getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
