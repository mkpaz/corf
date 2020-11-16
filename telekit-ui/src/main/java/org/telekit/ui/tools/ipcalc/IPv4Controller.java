package org.telekit.ui.tools.ipcalc;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.telecom.net.IP4Address;
import org.telekit.base.telecom.net.IP4Subnet;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.Dialogs;
import org.telekit.base.ui.IconCache;
import org.telekit.base.ui.UILoader;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.format.TextFormatters;
import org.telekit.ui.domain.FXMLView;

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
import static org.telekit.ui.MessageKeys.*;

public class IPv4Controller extends Controller {

    private static final String EMPTY_DATA = "n/a";
    private static final int MAX_SUBNET_BITS_TO_SPLIT = 15; // max 2 ^ 14 subnets = 16384
    private static final String DEFAULT_IP = "192.168.0.1";
    private static final int DEFAULT_NETMASK = 24;

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

    private IPv4ConverterController formatsConverterController = null;
    private static final List<Subnet> NETMASKS = createSubnetsList();

    @FXML
    public void initialize() {
        tfIPAddress.setTextFormatter(TextFormatters.ipv4Decimal());
        cmbNetmask.setButtonCell(new NetmaskCell());
        cmbNetmask.setCellFactory(property -> new NetmaskCell());
        cmbNetmask.setItems(observableArrayList(NETMASKS));
        cmbNetmask.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            fillSplitSelectors();
            int index = cmbNetmask.getSelectionModel().getSelectedIndex();
            Platform.runLater(() -> {
                tblNetmasks.scrollTo(index);
                tblNetmasks.getSelectionModel().select(index);
            });
        });

        cmbSplitSubnets.setButtonCell(new SplitSelectorCell());
        cmbSplitSubnets.setCellFactory(property -> new SplitSelectorCell());
        cmbSplitHosts.setButtonCell(new SplitSelectorCell());
        cmbSplitHosts.setCellFactory(property -> new SplitSelectorCell());

        cmbSplitSubnets.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int index = cmbSplitSubnets.getSelectionModel().getSelectedIndex();
                cmbSplitHosts.getSelectionModel().select(index);
                updateSplitDetails(newValue);
            }
        });
        cmbSplitHosts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int index = cmbSplitHosts.getSelectionModel().getSelectedIndex();
                cmbSplitSubnets.getSelectionModel().select(index);
            }
        });

        fillNetmasksTable();
        initSplitTable();

        tfIPAddress.setText(DEFAULT_IP);
        cmbNetmask.getSelectionModel().select(32 - DEFAULT_NETMASK);

        updateInfo();
    }

    @FXML
    public void updateInfo() {
        IP4Address address;
        IP4Subnet subnet;
        final TextBuilder tb = new TextBuilder();
        final int padding = 12;

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

        tb.appendLine("NETWORK:");
        tb.newLine();
        tb.appendLine(rightPad("Address:", padding),
                      defaultString(subnet.getNetworkAddress(), EMPTY_DATA));
        tb.appendLine(rightPad("Netmask:", padding),
                      subnet.getNetmask().toString(), " / ", subnet.getNetmask().toHexString("."));
        tb.appendLine(rightPad("Bitmask:", padding),
                      String.valueOf(subnet.getPrefixLength()));
        tb.appendLine(rightPad("Hosts:", padding),
                      subnet.getMinHost().toString(), " - ", subnet.getMaxHost().toString());
        tb.appendLine(rightPad("Available:", padding),
                      formatCount(subnet.getNumberOfHosts()), " address(es)");
        tb.appendLine(rightPad("Broadcast:", padding),
                      defaultString(subnet.getBroadcast(), EMPTY_DATA));
        tb.appendLine(rightPad("Wildcard:", padding),
                      subnet.getNetmask().reverseBytes().toString());

        tb.appendLine(rightPad("Remarks:", padding), "class " + subnet.getNetworkClass() + "-based;");
        List<String> remarks = new ArrayList<>();
        if (subnet.isLoopback()) remarks.add("localhost");
        if (subnet.isLinkLocal()) remarks.add("link-local (APIPA)");
        if (subnet.isMulticast()) remarks.add("multicast");
        if (subnet.isPrivate()) remarks.add("private network (RFC1918)");
        remarks.forEach(remark -> tb.appendLine(" ".repeat(padding) + remark + ";"));

        tb.newLine();

        tb.appendLine("FORMATS:");
        tb.newLine();
        tb.appendLine(rightPad("Integer:", padding),
                      String.valueOf(address.longValue()));
        tb.appendLine(rightPad("Binary:", padding),
                      address.toBinaryString("."));
        tb.appendLine(rightPad("Hex:", padding),
                      address.toHexString("."));

        taDetails.setText(tb.toString());
    }

    @FXML
    public void showFormatConverter() {
        IPv4ConverterController converterController = getOrCreateConverterDialog();
        converterController.setData(getEnteredAddress().longValue());
        converterController.getStage().showAndWait();
    }

    private IPv4ConverterController getOrCreateConverterDialog() {
        if (this.formatsConverterController != null) return this.formatsConverterController;

        Controller controller = UILoader.load(FXMLView.IPV4_CONV.getLocation(), Messages.getInstance());
        Stage dialog = Dialogs.modal(controller.getParent())
                .owner(rootPane.getScene().getWindow(), true)
                .title(Messages.get(TOOLS_IPCALC_TASK_REPORT))
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build();
        controller.setStage(dialog);
        this.formatsConverterController = (IPv4ConverterController) controller;

        return this.formatsConverterController;
    }

    @FXML
    public void saveToFile() {
        List<Subnet> subnets = tblSplit.getItems();
        File outputFile = Dialogs.file()
                .addFilter(Messages.get(FILE_DIALOG_TEXT), "*.txt")
                .initialFilename(FileUtils.sanitizeFileName("subnets.txt"))
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

    @Override
    public void reset() {}

    private void initSplitTable() {
        ObservableList<TableColumn<Subnet, ?>> columns = tblSplit.getColumns();

        @SuppressWarnings("unchecked")
        TableColumn<Subnet, String> indexColumn = (TableColumn<Subnet, String>) columns.get(0);

        indexColumn.setCellFactory(col -> {
            TableCell<Subnet, String> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.createStringBinding(() -> {
                if (cell.isEmpty()) {
                    return null;
                } else {
                    return Integer.toString(cell.getIndex() + 1);
                }
            }, cell.emptyProperty(), cell.indexProperty()));

            return cell;
        });

        for (TableColumn<Subnet, ?> column : columns) {
            if (isEmpty(column.getId())) continue;
            String propertyName = extractPropertyNameFromColumnID(column.getId());
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
    }

    private void fillSplitSelectors() {
        IP4Subnet subnet = getEnteredSubnet();

        if (subnet.getTrailingBitCount() <= 1) {
            cmbSplitSubnets.setItems(observableArrayList());
            cmbSplitHosts.setItems(observableArrayList());
            tfBitUsage.setText(getBitsUsage(subnet, 0));
            tblSplit.setItems(observableArrayList());
            return;
        }

        // limit max number of subnets to which netmask can be splitted
        // otherwise we need somehow to store 2 ^ 30 objects and show at least some of them in the table
        int splitSize = Math.min(subnet.getTrailingBitCount(), MAX_SUBNET_BITS_TO_SPLIT);

        List<Pair<Integer, Integer>> pairs = getPairsOfGivenSum(splitSize);
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

    private void updateSplitDetails(int subnetBits) {
        IP4Subnet subnet = getEnteredSubnet();
        tfBitUsage.setText(getBitsUsage(subnet, subnetBits));
        List<Subnet> splitResult = subnet.split(subnetBits).stream()
                .map(Subnet::new)
                .collect(Collectors.toList());
        tblSplit.setItems(observableArrayList(splitResult));
    }

    private void fillNetmasksTable() {
        tblNetmasks.setItems(observableArrayList(NETMASKS));
        ObservableList<TableColumn<Subnet, ?>> columns = tblNetmasks.getColumns();
        for (TableColumn<Subnet, ?> column : columns) {
            if (isEmpty(column.getId())) continue;
            String propertyName = extractPropertyNameFromColumnID(column.getId());
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
    }

    private String extractPropertyNameFromColumnID(String id) {
        return StringUtils.uncapitalize(id.substring(2));
    }

    private IP4Address getEnteredAddress() {
        return new IP4Address(trim(tfIPAddress.getText()));
    }

    private IP4Subnet getEnteredSubnet() {
        IP4Address address = getEnteredAddress();
        int prefixLength = 32 - cmbNetmask.getSelectionModel().getSelectedIndex();
        return new IP4Subnet(address, prefixLength);
    }

    private String getBitsUsage(IP4Subnet subnet, int subnetBits) {
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

    private static List<Pair<Integer, Integer>> getPairsOfGivenSum(int sum) {
        // result excludes 0 and bidirectional (a & b combo isn't unique e.g. [1, 2] & [2, 1])
        List<Pair<Integer, Integer>> result = new ArrayList<>(sum - 1);
        for (int i = 1; i < sum; i++) {
            result.add(new ImmutablePair<>(i, sum - i));
        }
        return result;
    }

    private static List<Subnet> createSubnetsList() {
        List<Subnet> result = new ArrayList<>();
        for (int prefixLength = 1; prefixLength <= 32; prefixLength++) {
            result.add(new Subnet(new IP4Subnet(IP4Subnet.NETMASKS[prefixLength - 1] + "/" + prefixLength)));
        }
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }

    private static String formatCount(long count) {
        if (count >= 1024 * 1024) {
            return count / (1024 * 1024) + "M";
        } else if (count >= 1024) {
            return count / (1024) + "K";
        } else {
            return String.valueOf(count);
        }
    }

    private static String defaultString(Object obj, String defaultString) {
        if (obj == null) return defaultString;
        String result = String.valueOf(obj);
        return isNotEmpty(result) ? result : defaultString;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Subnet {

        private final IP4Subnet ip4Subnet;

        public Subnet(IP4Subnet ip4Subnet) {
            this.ip4Subnet = ip4Subnet;
        }

        public String getNetworkAddress() {
            return defaultString(ip4Subnet.getNetworkAddress(), EMPTY_DATA);
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
            return formatCount(ip4Subnet.getNumberOfHosts());
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
                setText(formatCount(count));
            } else {
                setText(null);
            }
        }
    }
}
