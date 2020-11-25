package org.telekit.base.telecom.ip;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;

import static inet.ipaddr.IPAddress.IPVersion.IPV4;
import static org.telekit.base.util.StringUtils.splitEqually;

public class IP4Address {

    public static final long MIN_VALUE = 0;
    public static final long MAX_VALUE = Long.parseLong("1".repeat(32), 2);
    public static final String PATTERN = createIPv4Pattern();

    private final IPv4Address ip;

    public IP4Address(String addr) {
        try {
            this.ip = new IPAddressString(addr).toAddress(IPV4)
                    .toIPv4()
                    .withoutPrefixLength();
        } catch (AddressStringException e) {
            throw new IllegalArgumentException("Invalid IP address format [" + addr + "]");
        }
    }

    public IP4Address(int ip) {
        this.ip = new IPv4Address(ip);
    }

    protected IP4Address(IPv4Address ip) {
        this.ip = new IPv4Address(ip.intValue());
    }

    public IP4Address reverseBytes() {
        return new IP4Address(ip.reverseBytes());
    }

    public int intValue() {
        return ip.intValue();
    }

    public long longValue() {
        return ip.longValue();
    }

    public String toString() {
        return ip.toCanonicalString();
    }

    public String toBinaryString() {
        return ip.toBinaryString();
    }

    public String toBinaryString(String delimiter) {
        return String.join(delimiter, splitEqually(toBinaryString(), 8));
    }

    public String toHexString() {
        return ip.toHexString(false).toUpperCase();
    }

    public String toHexString(String delimiter) {
        return String.join(delimiter, splitEqually(toHexString(), 2));
    }

    private static String createIPv4Pattern() {
        String octet = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentOctet = "(\\." + octet + ")";
        return "^" + octet + "?" + subsequentOctet + "{0,3}";
    }
}
