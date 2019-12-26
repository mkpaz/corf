package corf.base.net;

import org.junit.jupiter.api.Test;
import corf.base.OrdinaryTest;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
public class IPv4HostTest {

    @Test
    public void testNetmaskDoesNotAffectToStringMethod() {
        String ipNoNetmask = "192.168.1.1";
        String ipCIDR = "192.168.1.1/27";
        IPv4Host ip4Plain = new IPv4Host(ipNoNetmask);
        IPv4Host ip4CIDR = new IPv4Host(ipCIDR);

        assertThat(ip4Plain.toString()).isEqualTo(ipNoNetmask);
        assertThat(ip4CIDR.toString()).isEqualTo(ipNoNetmask);
    }
}
