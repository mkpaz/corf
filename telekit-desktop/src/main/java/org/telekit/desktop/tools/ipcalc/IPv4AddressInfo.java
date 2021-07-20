package org.telekit.desktop.tools.ipcalc;

import org.telekit.base.telecom.IPv4AddressWrapper;

import java.util.Objects;

public final class IPv4AddressInfo {

    private final IPv4AddressWrapper ip;

    public IPv4AddressInfo(IPv4AddressWrapper ip) {
        this.ip = Objects.requireNonNull(ip);
    }

    public String getDecimalString() { return Objects.toString(ip.longValue(), ""); }

    public String getBinaryString() { return ip.toBinaryString("."); }

    public String getHexString() { return ip.toHexString("."); }

    public String getIPv4MappedAddress() { return ip.toIPv4MappedAddress(); }

    public String getReverseDNSLookupString() { return ip.toReverseDNSLookupString(); }
}
