package org.telekit.base.telecom.ip;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static inet.ipaddr.IPAddress.IPVersion.IPV4;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.base.telecom.ip.IPv4AddressWrapper.PARSE_PARAMS;

/** Represents IPv4 network: zero host plus prefix length */
public final class IPv4NetworkWrapper {

    public static final String[] NETMASKS = {
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
    };

    public static final int LINK_LOCAL_PREFIX_LEN = 31;
    public static final int MAX_PREFIX_LEN = 32;

    private final IPv4Address ipv4;

    public IPv4NetworkWrapper(String strValue) {
        try {
            IPv4Address ipv4 = new IPAddressString(strValue, PARSE_PARAMS)
                    .toAddress(IPV4)
                    .toIPv4();
            this.ipv4 = ipv4.toZeroHost();
        } catch (AddressStringException e) {
            throw new IllegalArgumentException("Invalid IP network [" + strValue + "]");
        }
    }

    public IPv4NetworkWrapper(IPv4AddressWrapper address, int prefixLength) {
        IPv4Address ipv4 = new IPv4Address(address.intValue(), prefixLength);
        this.ipv4 = ipv4.toZeroHost();
    }

    IPv4NetworkWrapper(IPv4Address ipv4) {
        this.ipv4 = ipv4;
    }

    /** Returns the first address in this subnet */
    public IPv4AddressWrapper getHostAddress() {
        return new IPv4AddressWrapper(ipv4);
    }

    /**
     * Returns address of this network.
     * <p>
     * Do not confuse with {@code getHostAddress()}. The host address can't be null,
     * because any network has the first IP address. While address of network itself
     * can be null, because "/31" and "/32" are not networks but peer-to-peer and host
     * respectively.
     */
    public @Nullable IPv4AddressWrapper getNetworkAddress() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN || ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
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

    public IPv4AddressWrapper getNetmask() {
        return new IPv4AddressWrapper(ipv4.getNetwork().getNetworkMask(ipv4.getNetworkPrefixLength()));
    }

    public IPv4AddressWrapper getMinHost() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN || ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
            return new IPv4AddressWrapper(ipv4);
        }
        return new IPv4AddressWrapper(ipv4.increment(1));
    }

    public IPv4AddressWrapper getMaxHost() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN) { return new IPv4AddressWrapper(ipv4.toMaxHost()); }
        if (ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) { return new IPv4AddressWrapper(ipv4); }
        return new IPv4AddressWrapper(ipv4.toMaxHost().increment(-1));
    }

    public @Nullable IPv4AddressWrapper getBroadcast() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN || ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) {
            return null;
        }
        return new IPv4AddressWrapper(ipv4.toMaxHost());
    }

    public @Nullable String getNetworkClass() {
        String binary = ipv4.toBinaryString();
        if (binary.startsWith("1111")) { return "E"; }
        if (binary.startsWith("1110")) { return "D"; }
        if (binary.startsWith("110")) { return "C"; }
        if (binary.startsWith("10")) { return "B"; }
        if (binary.startsWith("0")) { return "A"; }
        return null;
    }

    public long getTotalHostCount() {
        return (long) Math.pow(2, getTrailingBitCount());
    }

    public long getUsableHostCount() {
        if (ipv4.getNetworkPrefixLength() == LINK_LOCAL_PREFIX_LEN) { return 2; }
        if (ipv4.getNetworkPrefixLength() == MAX_PREFIX_LEN) { return 1; }
        return getTotalHostCount() - 2;
    }

    public List<IPv4NetworkWrapper> split(int subnetBits) {
        IPv4Address net = ipv4.toPrefixBlock();
        int length = net.getPrefixLength() + subnetBits;
        IPAddress blocks = net.setPrefixLength(length, false);

        Iterator<? extends IPAddress> blocksIterator = blocks.prefixBlockIterator();
        List<IPv4NetworkWrapper> result = new ArrayList<>();

        while (blocksIterator.hasNext()) {
            result.add(new IPv4NetworkWrapper(blocksIterator.next().toIPv4()));
        }

        return result;
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

    public String toString() {
        return String.valueOf(ipv4);
    }

    public IPv4Address unwrap() { return ipv4; }
}