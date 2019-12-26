package org.telekit.base.net;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Scheme {

    HTTP, HTTPS, SOCKS4, SOCKS5, FTP, FTPS, SFTP, FILE;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    public static Set<String> collection(Scheme... schemes) {
        return Arrays.stream(schemes)
                .map(Scheme::toString)
                .collect(Collectors.toSet());
    }
}
