package org.telekit.base.telecom.ip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.BaseSetup;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(BaseSetup.class)
public class IP4AddressTest {

    @Test
    public void testNetmaskDoesNotAffectToStringMethod() {
        String ipNoNetmask = "192.168.1.1";
        String ipCIDR = "192.168.1.1/27";
        IP4Address ip4Plain = new IP4Address(ipNoNetmask);
        IP4Address ip4CIDR = new IP4Address(ipCIDR);

        assertThat(ip4Plain.toString()).isEqualTo(ipNoNetmask);
        assertThat(ip4CIDR.toString()).isEqualTo(ipNoNetmask);
    }
}