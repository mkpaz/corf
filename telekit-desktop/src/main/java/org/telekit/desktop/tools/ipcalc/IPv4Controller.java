package org.telekit.desktop.tools.ipcalc;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.CancelEvent;
import org.telekit.base.i18n.Messages;
import org.telekit.base.telecom.ip.IP4Address;
import org.telekit.base.telecom.ip.IP4Subnet;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.IconCache;
import org.telekit.base.ui.UILoader;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.components.dialogs.Dialogs;
import org.telekit.controls.format.TextFormatters;
import org.telekit.desktop.domain.FXMLView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.base.util.StringUtils.splitEqually;
import static org.telekit.base.util.StringUtils.stringify;
import static org.telekit.controls.util.TableUtils.createIndexCellFactory;
import static org.telekit.desktop.MessageKeys.*;

public class IPv4Controller extends Controller {

    private static final String EMPTY_DATA = "n/a";
    private static final String DEFAULT_IP = "192.168.0.1";
    private static final int DEFAULT_NETMASK = 24;
    private static final int MAX_SUBNET_BITS_TO_SPLIT = 15; // 2 ^ 14 subnets = 16384
    private static final List<Subnet> NETMASKS = createSubnetList();
    private static final int NAME_PADDING = 12;

    public @FXML GridPane rootPane;
    public @FXML TextField tfIPAddress;
    public @FXML ComboBox<Subnet> cmbNetmask;
    public @FXML TextArea taDetails;
    public @FXML Button btnUpdate;
    public @FXML ComboBox<Integer> cmbSplitSubnets;
    public @FXML ComboBox<Integer> cmbSplitHosts;
    public @FXML TextField tfBitUsage;
    public @FXML TableView<Subnet> tblSplit;
    public @FXML Button btnSaveToFile;
    public @FXML TableView<Subnet> tblNetmasks;

    private IPv4ConverterController converterController = null;

    @FXML
    public void initialize() {
        tfIPAddress.setTextFormatter(TextFormatters.ipv4Decimal());

        initNetmasksSelectorAndTable();
        initSubnetSplitControls();

        // set initial data
        tfIPAddress.setText(DEFAULT_IP);
        cmbNetmask.getSelectionModel().select(32 - DEFAULT_NETMASK);
        updateDetailedInfo();
    }

    private void initNetmasksSelectorAndTable() {
        // netmasks dropdown
        cmbNetmask.setButtonCell(new NetmaskCell());
        cmbNetmask.setCellFactory(property -> new NetmaskCell());
        cmbNetmask.setItems(observableArrayList(NETMASKS));
        cmbNetmask.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            fillSplitSelectors();
            int index = cmbNetmask.getSelectionModel().getSelectedIndex();
            Platform.runLater(() -> {
                tblNetmasks.scrollTo(index);
                tblNetmasks.getSelectionModel().select(index);
            });
        });

        // fill netmasks table
        tblNetmasks.setItems(observableArrayList(NETMASKS));
        ObservableList<TableColumn<Subnet, ?>> columns = tblNetmasks.getColumns();
        for (TableColumn<Subnet, ?> column : columns) {
            if (isEmpty(column.getId())) continue;
            String propertyName = extractPropertyNameFromColumnID(column.getId());
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
    }

    @SuppressWarnings("unchecked")
    private void initSubnetSplitControls() {
        // subnets dropdown
        cmbSplitSubnets.setButtonCell(new SplitSelectorCell());
        cmbSplitSubnets.setCellFactory(property -> new SplitSelectorCell());
        cmbSplitSubnets.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int index = cmbSplitSubnets.getSelectionModel().getSelectedIndex();
                cmbSplitHosts.getSelectionModel().select(index);
                updateSplitControls(newValue);
            }
        });

        // hosts dropdown
        cmbSplitHosts.setButtonCell(new SplitSelectorCell());
        cmbSplitHosts.setCellFactory(property -> new SplitSelectorCell());
        cmbSplitHosts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int index = cmbSplitHosts.getSelectionModel().getSelectedIndex();
                cmbSplitSubnets.getSelectionModel().select(index);
            }
        });

        // result table
        ObservableList<TableColumn<Subnet, ?>> columns = tblSplit.getColumns();
        TableColumn<Subnet, String> indexColumn = (TableColumn<Subnet, String>) columns.get(0);
        indexColumn.setCellFactory(createIndexCellFactory());
        for (TableColumn<Subnet, ?> column : columns) {
            if (isEmpty(column.getId())) continue;
            String propertyName = extractPropertyNameFromColumnID(column.getId());
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
    }

    private void updateSplitControls(int subnetBits) {
        IP4Subnet subnet = getEnteredSubnet();
        tfBitUsage.setText(bitsUsageOf(subnet, subnetBits));
        List<Subnet> splitResult = subnet.split(subnetBits).stream()
                .map(Subnet::new)
                .collect(Collectors.toList());
        tblSplit.setItems(observableArrayList(splitResult));
    }

    @FXML
    public void updateDetailedInfo() {
        IP4Address address;
        IP4Subnet subnet;
        TextBuilder text = new TextBuilder();

        try {
            address = getEnteredAddress();
            subnet = getEnteredSubnet();
        } catch (Exception e) {
            taDetails.setText(Messages.get(TOOLS_IPCALC_MSG_INVALID_IP_ADDRESS));
            return;
        }

        Objects.requireNonNull(address);
        Objects.requireNonNull(subnet);

        fillSplitSelectors();

        text.appendLine("NETWORK:");
        text.newLine();
        text.appendLine(pad("Address:"), stringify(subnet.getNetworkAddress(), EMPTY_DATA));
        text.appendLine(pad("Netmask:"), subnet.getNetmask().toString(), " / ", subnet.getNetmask().toHexString("."));
        text.appendLine(pad("Bitmask:"), stringify(subnet.getPrefixLength()));
        text.appendLine(pad("Hosts:"), subnet.getMinHost().toString(), " - ", subnet.getMaxHost().toString());
        text.appendLine(pad("Available:"), formatHostsCount(subnet.getNumberOfHosts()), " address(es)");
        text.appendLine(pad("Broadcast:"), stringify(subnet.getBroadcast(), EMPTY_DATA));
        text.appendLine(pad("Wildcard:"), subnet.getNetmask().reverseBytes().toString());

        text.appendLine(pad("Remarks:"), "class " + subnet.getNetworkClass() + "-based;");
        List<String> remarks = new ArrayList<>();
        if (subnet.isLoopback()) remarks.add("localhost");
        if (subnet.isLinkLocal()) remarks.add("link-local (APIPA)");
        if (subnet.isMulticast()) remarks.add("multicast");
        if (subnet.isPrivate()) remarks.add("private network (RFC1918)");
        remarks.forEach(remark -> text.appendLine(" ".repeat(NAME_PADDING) + remark + ";"));
        text.newLine();

        text.appendLine("FORMATS:");
        text.newLine();
        text.appendLine(pad("Integer:"), stringify(address.longValue()));
        text.appendLine(pad("Binary:"), address.toBinaryString("."));
        text.appendLine(pad("Hex:"), address.toHexString("."));

        taDetails.setText(text.toString());
    }

    @FXML
    public void showConverterDialog() {
        IPv4ConverterController converterController = getOrCreateConverterDialog();
        converterController.setData(getEnteredAddress().longValue());
        Dialogs.showAndWait(converterController);
    }

    private IPv4ConverterController getOrCreateConverterDialog() {
        if (converterController != null) {
            converterController.reset();
            return converterController;
        }

        Controller controller = UILoader.load(FXMLView.IPV4_CONV.getLocation(), Messages.getInstance());
        controller.subscribe(CancelEvent.class, event -> Dialogs.hide(controller));

        Dialogs.modal(controller.getParent(), rootPane.getScene().getWindow())
                .title(Messages.get(TOOLS_IPCALC_TASK_REPORT))
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build();

        converterController = (IPv4ConverterController) controller;
        return this.converterController;
    }

    @FXML
    public void saveSplitResultToFile() {
        List<Subnet> subnets = tblSplit.getItems();
        File outputFile = Dialogs.fileChooser()
                .addFilter(Messages.get(FILE_DIALOG_TEXT), "*.txt")
                .initialFileName(FileUtils.sanitizeFileName("subnets.txt"))
                .build()
                .showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null || subnets.isEmpty()) return;

        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw)) {

            for (Subnet subnet : subnets) {
                out.write(subnet.getNetworkAddress());
                out.write("\n");
            }
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }

    private void fillSplitSelectors() {
        IP4Subnet subnet = getEnteredSubnet();

        if (subnet.getTrailingBitCount() <= 1) {
            cmbSplitSubnets.setItems(observableArrayList());
            cmbSplitHosts.setItems(observableArrayList());
            tfBitUsage.setText(bitsUsageOf(subnet, 0));
            tblSplit.setItems(observableArrayList());
            return;
        }

        // limit max number of subnets to split, otherwise we need somehow to store 2^30
        // objects and show at least some of them in the table
        int splitSize = Math.min(subnet.getTrailingBitCount(), MAX_SUBNET_BITS_TO_SPLIT);

        List<Pair<Integer, Integer>> pairs = findPairsOfGivenSum(splitSize);
        ObservableList<Integer> subnets = observableArrayList();
        ObservableList<Integer> hosts = observableArrayList();
        for (Pair<Integer, Integer> pair : pairs) {
            subnets.add(pair.getLeft());
            hosts.add(pair.getRight());
        }

        cmbSplitSubnets.setItems(subnets);
        cmbSplitSubnets.getSelectionModel().selectFirst();
        cmbSplitHosts.setItems(hosts);
        cmbSplitHosts.getSelectionModel().selectFirst();
    }

    private String extractPropertyNameFromColumnID(String colID) {
        // fail fast
        if (isBlank(colID)) throw new IllegalArgumentException("Invalid column ID");
        // all column ids start with "col", e.g. "colBroadcast"
        // the latter part must be identical to corresponding field name
        return StringUtils.uncapitalize(colID.substring(3));
    }

    private IP4Address getEnteredAddress() {
        return new IP4Address(trim(tfIPAddress.getText()));
    }

    private IP4Subnet getEnteredSubnet() {
        IP4Address address = getEnteredAddress();
        int prefixLength = 32 - cmbNetmask.getSelectionModel().getSelectedIndex();
        return new IP4Subnet(address, prefixLength);
    }

    private String bitsUsageOf(IP4Subnet subnet, int subnetBits) {
        String bitsUsage;
        if (subnet.getPrefixLength() < 31) {
            bitsUsage = Objects.requireNonNull(subnet.getHostAddress()).toBinaryString().substring(0, subnet.getPrefixLength()) +
                    "s".repeat(subnetBits) +
                    "h".repeat(subnet.getTrailingBitCount() - subnetBits);
        } else {
            bitsUsage = subnet.getHostAddress().toBinaryString();
        }
        return String.join(".", splitEqually(bitsUsage, 8));
    }

    private static List<Pair<Integer, Integer>> findPairsOfGivenSum(int sum) {
        // result excludes "0" and reversed sets (e.g. [1, 2] and [2, 1] are the same)
        List<Pair<Integer, Integer>> result = new ArrayList<>(sum - 1);
        for (int i = 1; i < sum; i++) {
            result.add(new ImmutablePair<>(i, sum - i));
        }
        return result;
    }

    private static List<Subnet> createSubnetList() {
        List<Subnet> result = new ArrayList<>();
        for (int prefixLength = 1; prefixLength <= 32; prefixLength++) {
            result.add(new Subnet(new IP4Subnet(IP4Subnet.NETMASKS[prefixLength - 1] + "/" + prefixLength)));
        }
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }

    private static String formatHostsCount(long count) {
        if (count >= 1024 * 1024) {
            return count / (1024 * 1024) + "M";
        } else if (count >= 1024) {
            return count / (1024) + "K";
        } else {
            return String.valueOf(count);
        }
    }

    private static String pad(String name) {
        return rightPad(name, NAME_PADDING);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Subnet {

        private final IP4Subnet ip4Subnet;

        public Subnet(IP4Subnet ip4Subnet) {
            this.ip4Subnet = ip4Subnet;
        }

        public String getNetworkAddress() {
            return stringify(ip4Subnet.getNetworkAddress(), EMPTY_DATA);
        }

        public String getPrefixLength() {
            return String.valueOf(ip4Subnet.getPrefixLength());
        }

        public String getMinHost() {
            return ip4Subnet.getMinHost().toString();
        }

        public String getMaxHost() {
            return ip4Subnet.getMaxHost().toString();
        }

        public String getBroadcast() {
            IP4Address address = ip4Subnet.getBroadcast();
            return address != null ? address.toString() : EMPTY_DATA;
        }

        public String getNetmaskDecimal() {
            return ip4Subnet.getNetmask().toString();
        }

        public String getNetmaskHex() {
            return ip4Subnet.getNetmask().toHexString(".");
        }

        public String getNumberOfHosts() {
            return formatHostsCount(ip4Subnet.getNumberOfHosts());
        }

        public String getNetmaskWildcard() {
            return ip4Subnet.getNetmask().reverseBytes().toString();
        }

        public IP4Subnet unwrap() {
            return ip4Subnet;
        }
    }

    public static class NetmaskCell extends ListCell<Subnet> {

        @Override
        protected void updateItem(Subnet subnet, boolean empty) {
            super.updateItem(subnet, empty);

            if (subnet != null) {
                setText(subnet.getNetmaskDecimal() + "/" + subnet.getPrefixLength());
            } else {
                setText(null);
            }
        }
    }

    public static class SplitSelectorCell extends ListCell<Integer> {

        @Override
        protected void updateItem(Integer numberOfBits, boolean empty) {
            super.updateItem(numberOfBits, empty);

            if (numberOfBits != null) {
                long count = (long) Math.pow(2, numberOfBits);
                setText(formatHostsCount(count));
            } else {
                setText(null);
            }
        }
    }
}
