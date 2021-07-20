package org.telekit.base.telecom;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.BaseSetup;
import org.telekit.base.telecom.IPv4AddressWrapper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(BaseSetup.class)
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

    @Test
    void name() throws AddressStringException {
        IPAddress hostName = new IPAddressString("192.168.*").toAddress();
        System.out.println(hostName.contains(new IPAddressString("192.169.1.1").toAddress()));
    }
}