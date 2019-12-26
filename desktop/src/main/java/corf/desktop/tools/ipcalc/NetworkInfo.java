package corf.desktop.tools.ipcalc;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.net.IPv4Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static corf.base.net.IPv4Network.LINK_LOCAL_PREFIX_LEN;
import static corf.desktop.tools.ipcalc.IPv4CalcViewModel.NUMBER_FORMAT;

public final class NetworkInfo {

    static final int MAX_SUBNET_BITS_TO_SPLIT = 15; // 2 ^ 14 subnets = 16384

    private final IPv4Network network;

    public NetworkInfo(IPv4Network network) {
        this.network = Objects.requireNonNull(network, "network");
    }

    public String getNetworkAddress() {
        return Objects.toString(network.getNetworkAddress(), "");
    }

    public int getPrefixLength() {
        return network.getPrefixLength();
    }

    public String getMinHost() {
        return network.getMinHost().toString();
    }

    public String getMaxHost() {
        return network.getMaxHost().toString();
    }

    public String getBroadcast() {
        return Objects.toString(network.getBroadcast(), "");
    }

    public long getTotalHostCount() {
        return network.getTotalHostCount();
    }

    public String getTotalHostCountFormatted() {
        return NUMBER_FORMAT.format(network.getTotalHostCount());
    }

    public long getUsableHostCount() {
        return network.getUsableHostCount();
    }

    public @Nullable String getNetworkClass() {
        return network.getNetworkClass();
    }

    public String getNetmaskAsDecimal() {
        return network.getNetmask().toString();
    }

    public String getNetmaskAsHex() {
        return network.getNetmask().toHexString(".");
    }

    public String getNetmaskAsBinary() {
        return network.getNetmask().toBinaryString(".");
    }

    public String getWildcardMask() {
        return network.getNetmask().reverseBytes().toString();
    }

    public Stream<String> getAllHosts() {
        return network.stream().map(String::valueOf);
    }

    public BitUsage getBitUsage(int subnetBitCount) {
        // nothing to split
        if (network.getPrefixLength() >= LINK_LOCAL_PREFIX_LEN) {
            return new BitUsage(network.getHostAddress().toBinaryString(), 0, 0);
        }

        String hostAddressBinary = network.getHostAddress()
                .toBinaryString()
                .substring(0, network.getPrefixLength());

        return new BitUsage(hostAddressBinary, subnetBitCount, network.getTrailingBitCount() - subnetBitCount);
    }

    public List<SplitOption> getSplitOptions() {
        if (network.getTrailingBitCount() <= 1) { return Collections.emptyList(); }

        return findPairsOfGivenSum(network.getTrailingBitCount()).stream()
                .map(pair -> new SplitOption(pair[0], pair[1]))
                // limit max number create subnets to split, otherwise we'd need to store 2^30 objects
                .filter(variant -> variant.subnetBitCount() <= MAX_SUBNET_BITS_TO_SPLIT)
                .collect(Collectors.toList());
    }

    public List<NetworkInfo> split(int subnetBitCount) {
        return network.split(subnetBitCount).stream()
                .map(NetworkInfo::new)
                .collect(Collectors.toList());
    }

    public List<String> getExtraInfo() {
        List<String> result = new ArrayList<>();

        // @formatter:off
        if (network.isLoopback())  { result.add("localhost");                 }
        if (network.isLinkLocal()) { result.add("link-local (APIPA)");        }
        if (network.isMulticast()) { result.add("multicast");                 }
        if (network.isPrivate())   { result.add("private network (RFC1918)"); }
        // @formatter:on

        return result;
    }

    private List<int[]> findPairsOfGivenSum(int sum) {
        // excluding '0' and reversed pairs, e.g. [1, 2] and [2, 1]
        var pairs = new ArrayList<int[]>(sum - 1);
        for (int i = 1; i < sum; i++) {
            pairs.add(new int[] { i, sum - i });
        }
        return pairs;
    }

    ///////////////////////////////////////////////////////////////////////////

    public record SplitOption(int subnetBitCount, int hostBitCount) {

        public long numberOfSubnets() {
            return (long) Math.pow(2, subnetBitCount);
        }

        public long numberOfHosts() {
            return (long) Math.pow(2, hostBitCount);
        }
    }

    public record BitUsage(String hostAddress, int subnetBitCount, int hostBitCount) {

        public static final Character SUBNET_CHAR = 'S';
        public static final Character HOST_CHAR = 'X';

        @Override
        public String toString() {
            String s = hostAddress
                    + StringUtils.repeat(SUBNET_CHAR, subnetBitCount)
                    + StringUtils.repeat(HOST_CHAR, hostBitCount);
            return String.join(".", corf.base.text.StringUtils.splitEqually(s, 8));
        }
    }
}
