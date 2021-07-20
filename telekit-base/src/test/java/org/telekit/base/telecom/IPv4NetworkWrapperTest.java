package org.telekit.base.telecom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.BaseSetup;
import org.telekit.base.telecom.IPv4NetworkWrapper;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * IPv4NetworkWrapper is just a wrapper around "com.github.seancfoley:ipaddress"
 * which is heavily tested itself. So, we only make some simple tests and assert
 * corner cases.
 */
@ExtendWith(BaseSetup.class)
class IPv4NetworkWrapperTest {

    @Test
    public void testValidSubnets() {
        testSubnet(
                new IPv4NetworkWrapper("192.168.1.1/27"),
                new ExpectedResult("192.168.1.0", "192.168.1.0", 27, 5, "192.168.1.1", "192.168.1.30", "192.168.1.31", 32)
        );

        testSubnet(
                new IPv4NetworkWrapper("192.168.1.0/27"),
                new ExpectedResult("192.168.1.0", "192.168.1.0", 27, 5, "192.168.1.1", "192.168.1.30", "192.168.1.31", 32)
        );

        // link-local
        testSubnet(
                new IPv4NetworkWrapper("192.168.1.21/31"),
                new ExpectedResult("192.168.1.20", null, 31, 1, "192.168.1.20", "192.168.1.21", null, 2)
        );

        // host
        testSubnet(
                new IPv4NetworkWrapper("192.168.1.33/32"),
                new ExpectedResult("192.168.1.33", null, 32, 0, "192.168.1.33", "192.168.1.33", null, 1)
        );
    }

    @Test
    public void testHostValueDoesNotAffectToStringMethod() {
        IPv4NetworkWrapper net0 = new IPv4NetworkWrapper("192.168.1.0/24");
        IPv4NetworkWrapper net1 = new IPv4NetworkWrapper("192.168.1.1/24");
        IPv4NetworkWrapper net100 = new IPv4NetworkWrapper("192.168.1.100/24");
        IPv4NetworkWrapper net255 = new IPv4NetworkWrapper("192.168.1.255/24");
        assertThat(net0.toString()).isEqualTo("192.168.1.0/24");
        assertThat(net1.toString()).isEqualTo(net0.toString());
        assertThat(net100.toString()).isEqualTo(net0.toString());
        assertThat(net255.toString()).isEqualTo(net0.toString());

        IPv4NetworkWrapper linkLocal0 = new IPv4NetworkWrapper("192.168.1.1/31");
        IPv4NetworkWrapper linkLocal1 = new IPv4NetworkWrapper("192.168.1.1/31");
        assertThat(linkLocal0.toString()).isEqualTo("192.168.1.0/31");
        assertThat(linkLocal1.toString()).isEqualTo(linkLocal0.toString());

        IPv4NetworkWrapper host = new IPv4NetworkWrapper("192.168.1.1/32");
        assertThat(host.toString()).isEqualTo("192.168.1.1/32");
    }

    public void testSubnet(IPv4NetworkWrapper actual, ExpectedResult expected) {
        assertThat(str(actual.getHostAddress())).isEqualTo(expected.hostAddress);
        assertThat(str(actual.getNetworkAddress())).isEqualTo(expected.networkAddress);
        assertThat(actual.getPrefixLength()).isEqualTo(expected.prefixLength);
        assertThat(actual.getTrailingBitCount()).isEqualTo(expected.trailingBitCount);
        assertThat(str(actual.getMinHost())).isEqualTo(expected.minHost);
        assertThat(str(actual.getMaxHost())).isEqualTo(expected.maxHost);
        assertThat(str(actual.getBroadcast())).isEqualTo(expected.broadcast);
        assertThat(actual.getTotalHostCount()).isEqualTo(expected.totalHostCount);
    }

    public static String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }

    public static class ExpectedResult {

        public final String hostAddress;
        public final String networkAddress;
        public final int prefixLength;
        public final int trailingBitCount;
        public final String minHost;
        public final String maxHost;
        public final String broadcast;
        public final long totalHostCount;

        public ExpectedResult(String hostAddress,
                              String networkAddress,
                              int prefixLength,
                              int trailingBitCount,
                              String minHost,
                              String maxHost,
                              String broadcast,
                              long totalHostCount
        ) {
            this.hostAddress = hostAddress;
            this.networkAddress = networkAddress;
            this.prefixLength = prefixLength;
            this.trailingBitCount = trailingBitCount;
            this.minHost = minHost;
            this.maxHost = maxHost;
            this.broadcast = broadcast;
            this.totalHostCount = totalHostCount;
        }
    }
}