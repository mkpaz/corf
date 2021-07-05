package org.telekit.desktop.tools.ipcalc;

import org.telekit.base.telecom.ip.IPv4AddressWrapper;
import org.telekit.base.telecom.ip.IPv4NetworkWrapper;
import org.telekit.base.util.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.telekit.base.telecom.ip.IPv4NetworkWrapper.LINK_LOCAL_PREFIX_LEN;
import static org.telekit.base.util.CommonUtils.map;
import static org.telekit.base.util.StringUtils.splitEqually;
import static org.telekit.desktop.tools.ipcalc.IPv4CalcViewModel.NUMBER_FORMAT;

@SuppressWarnings("unused")
public final class IPv4NetworkInfo {

    static final int MAX_SUBNET_BITS_TO_SPLIT = 15; // 2 ^ 14 subnets = 16384

    private final IPv4NetworkWrapper net;

    public IPv4NetworkInfo(IPv4NetworkWrapper net) {
        this.net = net;
    }

    public String getNetworkAddress() { return Objects.toString(net.getNetworkAddress(), ""); }

    public int getPrefixLength() { return net.getPrefixLength(); }

    public String getMinHost() { return net.getMinHost().toString(); }

    public String getMaxHost() { return net.getMaxHost().toString(); }

    public long getTotalHostCount() { return net.getTotalHostCount(); }

    public long getUsableHostCount() { return net.getUsableHostCount(); }

    public String getNetworkClass() { return net.getNetworkClass(); }

    public String getBroadcast() { return Objects.toString(CommonUtils.map(net.getBroadcast(), IPv4AddressWrapper::toString), ""); }

    public String getNetmaskAsDecimal() { return net.getNetmask().toString(); }

    public String getNetmaskAsHex() { return net.getNetmask().toHexString("."); }

    public String getNetmaskAsBinary() { return net.getNetmask().toBinaryString("."); }

    public String getWildcardMask() { return net.getNetmask().reverseBytes().toString(); }

    public String getTotalHostCountFormatted() { return NUMBER_FORMAT.format(net.getTotalHostCount()); }

    public BitUsage getBitUsage(int subnetBitCount) {
        if (net.getPrefixLength() >= LINK_LOCAL_PREFIX_LEN) {
            return new BitUsage(net.getHostAddress().toBinaryString(), 0, 0);
        }

        String hostAddressBinary = net.getHostAddress().toBinaryString().substring(0, net.getPrefixLength());
        return new BitUsage(hostAddressBinary, subnetBitCount, net.getTrailingBitCount() - subnetBitCount);
    }

    public List<SplitVariant> getSplitVariants() {
        if (net.getTrailingBitCount() <= 1) {
            return Collections.emptyList();
        }

        return findPairsOfGivenSum(net.getTrailingBitCount()).stream()
                .map(pair -> new SplitVariant(pair[0], pair[1]))
                // limit max number of subnets to split, otherwise we'd need to store 2^30 objects
                .filter(variant -> variant.subnetBitCount() <= MAX_SUBNET_BITS_TO_SPLIT)
                .collect(Collectors.toList());
    }

    public List<IPv4NetworkInfo> split(int subnetBitCount) {
        return net.split(subnetBitCount).stream()
                .map(IPv4NetworkInfo::new)
                .collect(Collectors.toList());
    }

    private List<int[]> findPairsOfGivenSum(int sum) {
        // excluding '0' and reversed pairs, e.g. [1, 2] and [2, 1]
        List<int[]> result = new ArrayList<>(sum - 1);
        for (int i = 1; i < sum; i++) {
            result.add(new int[]{i, sum - i});
        }
        return result;
    }

    public List<String> getAdditionalInfo() {
        List<String> result = new ArrayList<>();
        if (net.isLoopback()) { result.add("localhost"); }
        if (net.isLinkLocal()) { result.add("link-local (APIPA)"); }
        if (net.isMulticast()) { result.add("multicast"); }
        if (net.isPrivate()) { result.add("private network (RFC1918)"); }
        return result;
    }

    public Stream<String> getAllHostAddresses() {
        return net.stream().map(String::valueOf);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static record SplitVariant(int subnetBitCount, long hostBitCount) {

        public long numberOfSubnets() { return (long) Math.pow(2, subnetBitCount); }

        public long numberOfHosts() { return (long) Math.pow(2, hostBitCount); }
    }

    public static record BitUsage(String hostAddress, int subnetBitCount, int hostBitCount) {

        public static final Character SUBNET_CHAR = 'S';
        public static final Character HOST_CHAR = 'H';

        @Override
        public String toString() {
            String s = hostAddress + repeat(SUBNET_CHAR, subnetBitCount) + repeat(HOST_CHAR, hostBitCount);
            return String.join(".", splitEqually(s, 8));
        }
    }
}