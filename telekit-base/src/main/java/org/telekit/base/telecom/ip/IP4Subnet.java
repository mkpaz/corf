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

public class IP4Subnet {

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

    private static final int LINK_LOCAL_LEN = 31;
    private static final int MAX_LEN = 32;

    private final IPv4Address ip;

    public IP4Subnet(String addr) {
        try {
            IPv4Address ipv4 = new IPAddressString(addr).toAddress(IPV4).toIPv4();
            this.ip = ipv4.toZeroHost();
        } catch (AddressStringException e) {
            throw new IllegalArgumentException("Invalid subnet format [" + addr + "]");
        }
    }

    public IP4Subnet(IP4Address address, int prefixLength) {
        IPv4Address ipv4 = new IPv4Address(address.intValue(), prefixLength);
        this.ip = ipv4.toZeroHost();
    }

    protected IP4Subnet(IPv4Address ip) {
        this.ip = ip;
    }

    /**
     * Returns the first address in this subnet.
     */
    public IP4Address getHostAddress() {
        return new IP4Address(ip);
    }

    /**
     * Returns the network address of this subnet. Do not confuse with {@code getHostAddress()}.
     * The host address can't be null, because any subnet has the first IP address. But network
     * address can be null, because "/31" and "/32" aren't networks, they're peer-to-peer and host
     * respectively.
     */
    public @Nullable IP4Address getNetworkAddress() {
        if (ip.getNetworkPrefixLength() == LINK_LOCAL_LEN || ip.getNetworkPrefixLength() == MAX_LEN) {
            return null;
        }
        return new IP4Address(ip);
    }

    public int getPrefixLength() {
        return ip.getPrefixLength();
    }

    public int getTrailingBitCount() {
        return MAX_LEN - ip.getPrefixLength();
    }

    public IP4Address getNetmask() {
        return new IP4Address(ip.getNetwork().getNetworkMask(ip.getNetworkPrefixLength()));
    }

    public IP4Address getMinHost() {
        if (ip.getNetworkPrefixLength() == LINK_LOCAL_LEN || ip.getNetworkPrefixLength() == MAX_LEN) {
            return new IP4Address(ip);
        }
        return new IP4Address(ip.increment(1));
    }

    public IP4Address getMaxHost() {
        if (ip.getNetworkPrefixLength() == LINK_LOCAL_LEN) { return new IP4Address(ip.toMaxHost()); }
        if (ip.getNetworkPrefixLength() == MAX_LEN) { return new IP4Address(ip); }
        return new IP4Address(ip.toMaxHost().increment(-1));
    }

    public @Nullable IP4Address getBroadcast() {
        if (ip.getNetworkPrefixLength() == LINK_LOCAL_LEN || ip.getNetworkPrefixLength() == MAX_LEN) {
            return null;
        }
        return new IP4Address(ip.toMaxHost());
    }

    public @Nullable String getNetworkClass() {
        String binary = ip.toBinaryString();
        if (binary.startsWith("1111")) { return "E"; }
        if (binary.startsWith("1110")) { return "D"; }
        if (binary.startsWith("110")) { return "C"; }
        if (binary.startsWith("10")) { return "B"; }
        if (binary.startsWith("0")) { return "A"; }
        return null;
    }

    public long getNumberOfHosts() {
        return (long) Math.pow(2, getTrailingBitCount());
    }

    public List<IP4Subnet> split(int subnetBits) {
        IPv4Address net = ip.toPrefixBlock();
        int length = net.getPrefixLength() + subnetBits;
        IPAddress blocks = net.setPrefixLength(length, false);
        Iterator<? extends IPAddress> blocksIterator = blocks.prefixBlockIterator();
        List<IP4Subnet> result = new ArrayList<>();
        while (blocksIterator.hasNext()) {
            result.add(new IP4Subnet(blocksIterator.next().toIPv4()));
        }
        return result;
    }

    public boolean isLinkLocal() {
        return ip.isLinkLocal();
    }

    public boolean isLoopback() {
        return ip.isLoopback();
    }

    public boolean isMulticast() {
        return ip.isMulticast();
    }

    public boolean isPrivate() {
        return ip.isPrivate();
    }

    public String toString() {
        return String.valueOf(ip);
    }
}