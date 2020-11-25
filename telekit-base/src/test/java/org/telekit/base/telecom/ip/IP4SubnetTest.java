package org.telekit.base.telecom.ip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.BaseSetup;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IP4Subnet is just a wrapper around "com.github.seancfoley:ipaddress" which is heavily tested itself.
 * So, we only make some simple tests and assert corner cases.
 */
@ExtendWith(BaseSetup.class)
class IP4SubnetTest {

    @Test
    public void testValidSubnets() {
        testSubnet(
                new IP4Subnet("192.168.1.1/27"),
                new ExpectedResult("192.168.1.0", "192.168.1.0", 27, 5, "192.168.1.1", "192.168.1.30", "192.168.1.31", 32)
        );

        testSubnet(
                new IP4Subnet("192.168.1.0/27"),
                new ExpectedResult("192.168.1.0", "192.168.1.0", 27, 5, "192.168.1.1", "192.168.1.30", "192.168.1.31", 32)
        );

        // link-local
        testSubnet(
                new IP4Subnet("192.168.1.21/31"),
                new ExpectedResult("192.168.1.20", null, 31, 1, "192.168.1.20", "192.168.1.21", null, 2)
        );

        // host
        testSubnet(
                new IP4Subnet("192.168.1.33/32"),
                new ExpectedResult("192.168.1.33", null, 32, 0, "192.168.1.33", "192.168.1.33", null, 1)
        );
    }

    @Test
    public void testHostValueDoesNotAffectToStringMethod() {
        IP4Subnet subnet0 = new IP4Subnet("192.168.1.0/24");
        IP4Subnet subnet1 = new IP4Subnet("192.168.1.1/24");
        IP4Subnet subnet100 = new IP4Subnet("192.168.1.100/24");
        IP4Subnet subnet255 = new IP4Subnet("192.168.1.255/24");
        assertThat(subnet0.toString()).isEqualTo("192.168.1.0/24");
        assertThat(subnet1.toString()).isEqualTo(subnet0.toString());
        assertThat(subnet100.toString()).isEqualTo(subnet0.toString());
        assertThat(subnet255.toString()).isEqualTo(subnet0.toString());

        IP4Subnet linkLocal0 = new IP4Subnet("192.168.1.1/31");
        IP4Subnet linkLocal1 = new IP4Subnet("192.168.1.1/31");
        assertThat(linkLocal0.toString()).isEqualTo("192.168.1.0/31");
        assertThat(linkLocal1.toString()).isEqualTo(linkLocal0.toString());

        IP4Subnet host = new IP4Subnet("192.168.1.1/32");
        assertThat(host.toString()).isEqualTo("192.168.1.1/32");
    }

    public void testSubnet(IP4Subnet actual, ExpectedResult expected) {
        assertThat(str(actual.getHostAddress())).isEqualTo(expected.hostAddress);
        assertThat(str(actual.getNetworkAddress())).isEqualTo(expected.networkAddress);
        assertThat(actual.getPrefixLength()).isEqualTo(expected.prefixLength);
        assertThat(actual.getTrailingBitCount()).isEqualTo(expected.trailingBitCount);
        assertThat(str(actual.getMinHost())).isEqualTo(expected.minHost);
        assertThat(str(actual.getMaxHost())).isEqualTo(expected.maxHost);
        assertThat(str(actual.getBroadcast())).isEqualTo(expected.broadcast);
        assertThat(actual.getNumberOfHosts()).isEqualTo(expected.numberOfHosts);
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
        public final long numberOfHosts;

        public ExpectedResult(String hostAddress,
                              String networkAddress,
                              int prefixLength,
                              int trailingBitCount,
                              String minHost,
                              String maxHost,
                              String broadcast,
                              long numberOfHosts
        ) {
            this.hostAddress = hostAddress;
            this.networkAddress = networkAddress;
            this.prefixLength = prefixLength;
            this.trailingBitCount = trailingBitCount;
            this.minHost = minHost;
            this.maxHost = maxHost;
            this.broadcast = broadcast;
            this.numberOfHosts = numberOfHosts;
        }
    }
}