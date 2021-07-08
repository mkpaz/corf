package org.telekit.base.net.connection;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.domain.security.Credentials;

import java.net.InetSocketAddress;

public interface ConnectionParams {

    Scheme getScheme();

    String getHost();

    int getPort();

    @Nullable Credentials getCredentials();

    default InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }
}
