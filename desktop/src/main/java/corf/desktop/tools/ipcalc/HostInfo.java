package corf.desktop.tools.ipcalc;

import org.apache.commons.lang3.StringUtils;
import corf.base.net.IPv4Host;

import java.util.Objects;

public final class HostInfo {

    private final IPv4Host host;

    public HostInfo(IPv4Host host) {
        this.host = Objects.requireNonNull(host, "host");
    }

    public String getDecimalString() {
        return Objects.toString(host.longValue(), "");
    }

    public String getBinaryString() {
        return host.toBinaryString(".");
    }

    public String getHexString() {
        return host.toHexString(".");
    }

    public String getIPv4MappedAddress() {
        return StringUtils.defaultString(host.toIPv4MappedAddress());
    }

    public String getReverseDNSLookupString() {
        return host.toReverseDNSLookupString();
    }
}
