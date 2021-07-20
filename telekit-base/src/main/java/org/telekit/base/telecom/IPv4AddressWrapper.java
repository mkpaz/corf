package org.telekit.base.telecom;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringParameters;
import inet.ipaddr.ipv4.IPv4Address;

import static inet.ipaddr.IPAddress.IPVersion.IPV4;
import static org.telekit.base.util.StringUtils.splitEqually;

/** Represents single IPv4 address w/o prefix length info */
public final class IPv4AddressWrapper {

    public static final long MIN_VALUE = 0;
    public static final long MAX_VALUE = Long.parseLong("1".repeat(32), 2);
    public static final String PATTERN = createIPv4Pattern();

    static final IPAddressStringParameters PARSE_PARAMS =
            new IPAddressStringParameters.Builder()
                    .allow_inet_aton(false) // disallow partial formats like 192.168/16
                    .allowEmpty(false)
                    .allowSingleSegment(false)
                    .toParams();

    private final IPv4Address ipv4;

    public IPv4AddressWrapper(String strValue) {
        try {
            this.ipv4 = new IPAddressString(strValue, PARSE_PARAMS)
                    .toAddress(IPV4)
                    .toIPv4()
                    .withoutPrefixLength();
        } catch (AddressStringException e) {
            throw new IllegalArgumentException("Invalid IP address [" + strValue + "]");
        }
    }

    public IPv4AddressWrapper(int longValue) {
        this.ipv4 = new IPv4Address(longValue);
    }

    IPv4AddressWrapper(IPv4Address ipv4) {
        this.ipv4 = new IPv4Address(ipv4.intValue());
    }

    public IPv4AddressWrapper reverseBytes() {
        return new IPv4AddressWrapper(ipv4.reverseBytes());
    }

    public int intValue() {
        return ipv4.intValue();
    }

    public long longValue() {
        return ipv4.longValue();
    }

    public String toString() {
        return ipv4.toCanonicalString();
    }

    public String toBinaryString() {
        return ipv4.toBinaryString();
    }

    public String toBinaryString(String delimiter) {
        return String.join(delimiter, splitEqually(toBinaryString(), 8));
    }

    public String toHexString() {
        return ipv4.toHexString(false).toUpperCase();
    }

    public String toHexString(String delimiter) {
        return String.join(delimiter, splitEqually(toHexString(), 2));
    }

    public String toIPv4MappedAddress() {
        return ipv4.isIPv6Convertible() ? ipv4.getIPv4MappedAddress().toCanonicalString() : null;
    }

    public String toReverseDNSLookupString() {
        return ipv4.toReverseDNSLookupString();
    }

    public IPv4Address unwrap() {
        return ipv4;
    }

    public static boolean isValidString(String ipStr) {
        // ipaddress treat empty string as 127.0.0.1, nut it obviously should not be valid
        return new IPAddressString(ipStr, PARSE_PARAMS).isValid();
    }

    private static String createIPv4Pattern() {
        String octet = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentOctet = "(\\." + octet + ")";
        return "^" + octet + "?" + subsequentOctet + "{0,3}";
    }
}
