package corf.base.net;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static inet.ipaddr.IPAddress.IPVersion.IPV4;
import static corf.base.net.IPv4Host.PARSE_PARAMS;

/**
 * Represents IPv4 network: zero host plus prefix length.
 */
public final class IPv4Network {

    public static final int LINK_LOCAL_PREFIX_LEN = 31;
    public static final int MAX_PREFIX_LEN = 32;
    public static final List<String> NETMASKS = List.of(
            "128.0.0.0",
            "192.0.0.0",
            "224.0.0.0",
            "240.0.0.0",
            "248.0.0.0",
            "252.0.0.0",
            "254.0.0.0",
            "255.0.0.0",
            "255.128.0.0",
            "255.192.0.0",
            "255.224.0.0",
            "255.240.0.0",
            "255.248.0.0",
            "255.252.0.0",
            "255.254.0.0",
            "255.255.0.0",
            "255.255.128.0",
            "255.255.192.0",
            "255.255.224.0",
            "255.255.240.0",
            "255.255.248.0",
            "255.255.252.0",
            "255.255.254.0",
            "255.255.255.0",
            "255.255.255.128",
            "255.255.255.192",
            "255.255.255.224",
            "255.255.255.240",
            "255.255.255.248",
            "255.255.255.252",
            "255.255.255.254",
            "255.255.255.255"
    );

    private final IPv4Address ipv4;

    public IPv4Network(String str) {
        Objects.requireNonNull(str, "str");
        try {
            IPv4Address ipv4 = new IPAddressString(str, PARSE_PARAMS)
                    .toAddress(IPV4)
                    .toIPv4();
            this.ipv4 = ipv4.toZeroHost();
        } catch (AddressStringException e) {
            throw new IllegalArgumentException("Invalid IP network [" + str + "].");
        }
    }

    public IPv4Network(IPv4Host host, int prefixLen) {
        IPv4Address ipv4 = new IPv4Address(host.intValue(), prefixLen);
        this.ipv4 = ipv4.toZeroHost();
    }

    IPv4Network(IPv4Address ipv4) {
        this.ipv4 = ipv4;
    }

    /**
     * Returns the first address in this subnet.
     */
    public IPv4Host getHostAddress() {
        return new IPv4Host(ipv4);
    }

    /**
     * Returns the address of the network.
     * <p>
     * Do not confuse this with the {@code getHostAddress()}. The host address can't be null,
     * because any network has at least one IP address. While network address itself can be null,
     * because "/31" and "/32" are not networks, but peer-to-peer and single host, respectively.
     */
    public @Nullable IPv4Host getNetworkAddress() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN
                || ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
            return null;
        }
        return getHostAddress();
    }

    public int getPrefixLength() {
        return ipv4.getPrefixLength();
    }

    public int getTrailingBitCount() {
        return MAX_PREFIX_LEN - ipv4.getPrefixLength();
    }

    public IPv4Host getNetmask() {
        return new IPv4Host(ipv4.getNetwork().getNetworkMask(ipv4.getNetworkPrefixLength()));
    }

    public IPv4Host getMinHost() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN
                || ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
            return new IPv4Host(ipv4);
        }
        return new IPv4Host(ipv4.increment(1));
    }

    public IPv4Host getMaxHost() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN) { return new IPv4Host(ipv4.toMaxHost()); }
        if (ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) { return new IPv4Host(ipv4); }
        return new IPv4Host(ipv4.toMaxHost().increment(-1));
    }

    public @Nullable IPv4Host getBroadcast() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN
                || ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
            return null;
        }
        return new IPv4Host(ipv4.toMaxHost());
    }

    public long getTotalHostCount() {
        return (long) Math.pow(2, getTrailingBitCount());
    }

    public long getUsableHostCount() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN) { return 2; }
        if (ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) { return 1; }
        return getTotalHostCount() - 2;
    }

    public @Nullable String getNetworkClass() {
        var binary = ipv4.toBinaryString();
        if (binary.startsWith("1111")) { return "E"; }
        if (binary.startsWith("1110")) { return "D"; }
        if (binary.startsWith("110")) { return "C"; }
        if (binary.startsWith("10")) { return "B"; }
        if (binary.startsWith("0")) { return "A"; }
        return null;
    }

    public List<IPv4Network> split(int subnetBits) {
        IPv4Address network = ipv4.toPrefixBlock();
        int length = network.getPrefixLength() + subnetBits;
        IPAddress blocks = network.setPrefixLength(length, false);

        Iterator<? extends IPAddress> blocksIterator = blocks.prefixBlockIterator();
        var result = new ArrayList<IPv4Network>();

        while (blocksIterator.hasNext()) {
            result.add(new IPv4Network(blocksIterator.next().toIPv4()));
        }

        return result;
    }

    public Stream<IPv4Host> stream() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN) {
            return Stream.of(getMinHost(), getMaxHost());
        }
        if (ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
            return Stream.of(getMinHost());
        }

        IPAddressSeqRange range = getMinHost().unwrap().toSequentialRange(getMaxHost().unwrap());
        return range.stream().map(ip -> new IPv4Host(ip.toIPv4()));
    }

    public boolean isLinkLocal() {
        return ipv4.isLinkLocal();
    }

    public boolean isLoopback() {
        return ipv4.isLoopback();
    }

    public boolean isMulticast() {
        return ipv4.isMulticast();
    }

    public boolean isPrivate() {
        return ipv4.isPrivate();
    }

    @Override
    public String toString() {
        return String.valueOf(ipv4);
    }

    @SuppressWarnings("unused")
    public IPv4Address unwrap() {
        return ipv4;
    }
}
