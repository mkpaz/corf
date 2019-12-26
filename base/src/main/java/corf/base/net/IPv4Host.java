package corf.base.net;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringParameters;
import inet.ipaddr.ipv4.IPv4Address;
import org.jetbrains.annotations.Nullable;
import corf.base.text.StringUtils;

import java.util.Objects;

import static inet.ipaddr.IPAddress.IPVersion.IPV4;
import static corf.base.text.StringUtils.splitEqually;

/**
 * Represents single IPv4 address without prefix length info.
 */
public final class IPv4Host {

    public static final long MIN_VALUE = 0;
    public static final long MAX_VALUE = Long.parseLong("1".repeat(32), 2);
    public static final String PATTERN = createIPv4Pattern();

    static final IPAddressStringParameters PARSE_PARAMS = new IPAddressStringParameters.Builder()
            .allow_inet_aton(false)    // deny partial formats like 192.168/16
            .allowEmpty(false)         // deny zero-length strings like ""
            .allowSingleSegment(false) // deny an address to be specified as a single value, e.g. FFFFFFFF,
            .toParams();               // without the standard use of segments like 1.2.3.4

    private final IPv4Address ipv4;

    public IPv4Host(String str) {
        Objects.requireNonNull(str, "str");
        try {
            this.ipv4 = new IPAddressString(str, PARSE_PARAMS)
                    .toAddress(IPV4)
                    .toIPv4()
                    .withoutPrefixLength();
        } catch (AddressStringException e) {
            throw new IllegalArgumentException("Invalid IP address [" + str + "].");
        }
    }

    public IPv4Host(int longValue) {
        this.ipv4 = new IPv4Address(longValue);
    }

    IPv4Host(IPv4Address ipv4) {
        this.ipv4 = new IPv4Address(ipv4.intValue());
    }

    public IPv4Host reverseBytes() {
        return new IPv4Host(ipv4.reverseBytes());
    }

    public int intValue() {
        return ipv4.intValue();
    }

    public long longValue() {
        return ipv4.longValue();
    }

    @Override
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

    public String toHexString(String separator) {
        return String.join(separator, StringUtils.splitEqually(toHexString(), 2));
    }

    public @Nullable String toIPv4MappedAddress() {
        return ipv4.isIPv6Convertible() ? ipv4.getIPv4MappedAddress().toCanonicalString() : null;
    }

    public String toReverseDNSLookupString() {
        return ipv4.toReverseDNSLookupString();
    }

    public IPv4Address unwrap() {
        return ipv4;
    }

    public static boolean isValidString(String str) {
        if (str == null || str.isBlank()) { return false; }
        return new IPAddressString(str, PARSE_PARAMS).isValid();
    }

    private static String createIPv4Pattern() {
        String octet = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentOctet = "(\\." + octet + ")";
        return "^" + octet + "?" + subsequentOctet + "{0,3}";
    }
}
