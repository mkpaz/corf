package corf.desktop.tools.ipcalc;

import backbonefx.mvvm.ConsumerCommand;
import backbonefx.mvvm.ViewModel;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import corf.base.Env;
import corf.base.desktop.Async;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.base.net.IPv4Host;
import corf.base.net.IPv4Network;
import corf.desktop.i18n.DM;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static corf.base.i18n.I18n.t;

public class IPv4CalcViewModel implements ViewModel {

    static final NumberFormat NUMBER_FORMAT = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
    static final String DEFAULT_IP = "192.168.0.1";
    static final int DEFAULT_NETMASK = 24;
    static final List<NetworkInfo> NETMASKS = createNetmaskList();

    private final ExecutorService executorService;

    @Inject
    public IPv4CalcViewModel(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private IPv4Host getIPv4Host() {
        return new IPv4Host(StringUtils.trim(ipv4String.get()));
    }

    private IPv4Network getIPv4Network() {
        return new IPv4Network(getIPv4Host(), netmaskInfo.get().getPrefixLength());
    }

    private static List<NetworkInfo> createNetmaskList() {
        var netmasks = new ArrayList<NetworkInfo>();
        for (int prefixLen = 1; prefixLen <= 32; prefixLen++) {
            var network = new IPv4Network(IPv4Network.NETMASKS.get(prefixLen - 1) + "/" + prefixLen);
            netmasks.add(new NetworkInfo(network));
        }
        Collections.reverse(netmasks);
        return netmasks;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    // @formatter:off
    private final StringProperty ipv4String = new SimpleStringProperty();
    public StringProperty ipv4StringProperty() { return ipv4String; }

    private final ObjectProperty<NetworkInfo> netmaskInfo = new SimpleObjectProperty<>();
    public ObjectProperty<NetworkInfo> netmaskInfoProperty() { return netmaskInfo; }

    public ObjectBinding<HostInfo> hostInfoProperty() { return hostInfo; }
    private final ObjectBinding<HostInfo> hostInfo = Bindings.createObjectBinding(() -> {
        try {
            return new HostInfo(getIPv4Host());
        } catch (Exception e) {
            return null;
        }
    }, ipv4String);

    public ObjectBinding<NetworkInfo> networkInfoProperty() { return networkInfo; }
    private final ObjectBinding<NetworkInfo> networkInfo = Bindings.createObjectBinding(() -> {
        try {
            return new NetworkInfo(getIPv4Network());
        } catch (Exception e) {
            return null;
        }
    }, ipv4String, netmaskInfo);
    // @formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == exportAllHostsInNetworkCommand ==

    public ConsumerCommand<File> exportAllHostsInNetworkCommand() { return exportAllHostsInNetwork; }

    private final ConsumerCommand<File> exportAllHostsInNetwork = new ConsumerCommand<>(
            this::exportAllHostsInNetwork,
            networkInfoProperty().isNotNull()
    );

    private void exportAllHostsInNetwork(File outputFile) {
        final var network = networkInfoProperty().get();

        if (network.getUsableHostCount() > Math.pow(2, 16)) {
            Events.fire(Notification.warning(t(DM.TPL_MSG_LIST_IS_TOO_LARGE_TO_EXPORT)));
            return;
        }

        Runnable runnable = () -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), UTF_8);
                 BufferedWriter out = new BufferedWriter(writer)) {

                network.getAllHosts().forEach(ip -> {
                    try {
                        out.write(ip);
                        out.write("\n");
                    } catch (IOException e) {
                        throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
                    }
                });
            } catch (IOException e) {
                throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
            }
        };

        Async.with(runnable)
                .setOnSucceeded(v -> Env.setLastVisitedDir(outputFile))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }

    // == exportSplitTableCommand ==

    public ConsumerCommand<Pair<File, List<NetworkInfo>>> exportSplitTableCommand() { return exportSplitTable; }

    private final ConsumerCommand<Pair<File, List<NetworkInfo>>> exportSplitTable = new ConsumerCommand<>(this::exportSplitTable);

    private void exportSplitTable(Pair<File, List<NetworkInfo>> export) {
        File outputFile = export.getKey();
        List<NetworkInfo> items = export.getValue();

        Runnable runnable = () -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), UTF_8);
                 BufferedWriter out = new BufferedWriter(writer)) {

                out.write("Network Address;Start Host;End Host;Broadcast\n");

                for (var network : items) {
                    out.write(network.getNetworkAddress());
                    out.write(";");
                    out.write(network.getMinHost());
                    out.write(";");
                    out.write(network.getMaxHost());
                    out.write(";");
                    out.write(network.getBroadcast());
                    out.write("\n");
                }
            } catch (Exception e) {
                throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
            }
        };

        Async.with(runnable)
                .setOnSucceeded(v -> Env.setLastVisitedDir(outputFile))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }
}
