package org.telekit.base.telecom.ip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.BaseSetup;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(BaseSetup.class)
public class IPv4AddressWrapperTest {

    @Test
    public void testNetmaskDoesNotAffectToStringMethod() throws Exception {
        String ipNoNetmask = "192.168.1.1";
        String ipCIDR = "192.168.1.1/27";
        IPv4AddressWrapper ip4Plain = new IPv4AddressWrapper(ipNoNetmask);
        IPv4AddressWrapper ip4CIDR = new IPv4AddressWrapper(ipCIDR);

        assertThat(ip4Plain.toString()).isEqualTo(ipNoNetmask);
        assertThat(ip4CIDR.toString()).isEqualTo(ipNoNetmask);
    }
}