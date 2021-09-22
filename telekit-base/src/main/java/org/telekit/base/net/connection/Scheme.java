package org.telekit.base.net.connection;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public enum Scheme {

    FILE,
    FTP,
    FTPS,
    HTTP,
    HTTPS,
    SFTP,
    SNMP,
    SOCKS4,
    SOCKS5,
    SSH,
    TELNET;

    public int getWellKnownPort() {
        return switch (this) {
            case FILE -> -1;
            case FTP -> 21;
            case FTPS -> 990;
            case HTTP -> 80;
            case HTTPS -> 443;
            case SSH, SFTP -> 22;
            case SNMP -> 161;
            case SOCKS4, SOCKS5 -> 1080;
            case TELNET -> 23;
        };
    }

    public static @Nullable Scheme fromString(String s) {
        if (s == null) { return null; }
        for (Scheme scheme : Scheme.values()) {
            if (StringUtils.equalsIgnoreCase(scheme.toString(), s)) { return scheme; }
        }
        return null;
    }
}
