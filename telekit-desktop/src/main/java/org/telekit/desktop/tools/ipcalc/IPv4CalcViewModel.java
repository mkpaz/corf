package org.telekit.desktop.tools.ipcalc;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.telekit.base.desktop.mvvm.ViewModel;
import org.telekit.base.telecom.ip.IPv4AddressWrapper;
import org.telekit.base.telecom.ip.IPv4NetworkWrapper;

import javax.inject.Singleton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.text.NumberFormat.Style;
import static java.text.NumberFormat.getCompactNumberInstance;
import static org.apache.commons.lang3.StringUtils.trim;

@Singleton
public class IPv4CalcViewModel implements ViewModel {

    static final String DEFAULT_IP = "192.168.0.1";
    static final int DEFAULT_NETMASK = 24;
    static final NumberFormat NUMBER_FORMAT = getCompactNumberInstance(Locale.US, Style.SHORT);
    static final List<IPv4NetworkInfo> NETMASKS = createNetmaskList();

    private static List<IPv4NetworkInfo> createNetmaskList() {
        List<IPv4NetworkInfo> result = new ArrayList<>();
        for (int prefixLength = 1; prefixLength <= 32; prefixLength++) {
            result.add(new IPv4NetworkInfo(new IPv4NetworkWrapper(IPv4NetworkWrapper.NETMASKS[prefixLength - 1] + "/" + prefixLength)));
        }
        Collections.reverse(result);
        return result;
    }

    private IPv4AddressWrapper getIPv4Address() {
        return new IPv4AddressWrapper(trim(ipAddress.get()));
    }

    private IPv4NetworkWrapper getIPv4Network() {
        return new IPv4NetworkWrapper(getIPv4Address(), netmask.get().getPrefixLength());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    private final StringProperty ipAddress = new SimpleStringProperty(this, "ipAddress");

    public StringProperty ipAddressProperty() { return ipAddress; }

    private final ObjectProperty<IPv4NetworkInfo> netmask = new SimpleObjectProperty<>(this, "netmask");

    public ObjectProperty<IPv4NetworkInfo> netmaskProperty() { return netmask; }

    private final ObjectBinding<IPv4AddressInfo> ipAddressInfo = Bindings.createObjectBinding(() -> {
        try {
            return new IPv4AddressInfo(getIPv4Address());
        } catch (Exception e) {
            return null;
        }
    }, ipAddress);

    public ObjectBinding<IPv4AddressInfo> ipAddressInfoProperty() { return ipAddressInfo; }

    private final ObjectBinding<IPv4NetworkInfo> ipNetworkInfo = Bindings.createObjectBinding(() -> {
        try {
            return new IPv4NetworkInfo(getIPv4Network());
        } catch (Exception e) {
            return null;
        }
    }, ipAddress, netmask);

    public ObjectBinding<IPv4NetworkInfo> ipNetworkInfoProperty() { return ipNetworkInfo; }
}
