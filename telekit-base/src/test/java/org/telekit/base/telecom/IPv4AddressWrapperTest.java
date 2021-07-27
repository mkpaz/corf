package org.telekit.base.telecom;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.OrdinaryTest;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
public class IPv4AddressWrapperTest {

    @Test
    public void testNetmaskDoesNotAffectToStringMethod() {
        String ipNoNetmask = "192.168.1.1";
        String ipCIDR = "192.168.1.1/27";
        IPv4AddressWrapper ip4Plain = new IPv4AddressWrapper(ipNoNetmask);
        IPv4AddressWrapper ip4CIDR = new IPv4AddressWrapper(ipCIDR);

        assertThat(ip4Plain.toString()).isEqualTo(ipNoNetmask);
        assertThat(ip4CIDR.toString()).isEqualTo(ipNoNetmask);
    }
}