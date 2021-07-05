package org.telekit.desktop.tools.ipcalc;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.telecom.ip.IPv4AddressWrapper;
import org.telekit.base.util.DesktopUtils;
import org.telekit.base.util.FileUtils;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.*;
import org.telekit.desktop.tools.ipcalc.IPv4NetworkInfo.SplitVariant;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.util.FileUtils.getParentPath;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.menuItem;
import static org.telekit.controls.util.Tables.setColumnConstraints;
import static org.telekit.desktop.i18n.DesktopMessages.*;
import static org.telekit.desktop.tools.ipcalc.IPv4CalcViewModel.*;
import static org.telekit.desktop.tools.ipcalc.IPv4NetworkInfo.BitUsage.HOST_CHAR;
import static org.telekit.desktop.tools.ipcalc.IPv4NetworkInfo.BitUsage.SUBNET_CHAR;

@Singleton
public final class IPv4CalcView extends SplitPane implements Initializable, View<IPv4CalcViewModel> {

    Button ipConverterBtn;
    TextField ipText;
    ComboBox<IPv4NetworkInfo> netmaskChoice;
    TreeTableView<Pair<String, String>> detailsTable;

    ComboBox<SplitVariant> splitSubnetsChoice;
    BitUsageLabel bitUsageLabel;
    TableView<IPv4NetworkInfo> splitTable;
    Button splitExportBtn;
    TableView<IPv4NetworkInfo> netmaskTable;

    private final IPv4CalcViewModel model;
    private final Overlay overlay;
    private final ExecutorService threadPool;

    private IPv4ConverterDialog converterDialog;
    private Path lastVisitedDirectory;

    @Inject
    public IPv4CalcView(IPv4CalcViewModel model,
                        Overlay overlay,
                        ExecutorService threadPool) {
        this.model = model;
        this.overlay = overlay;
        this.threadPool = threadPool;

        createView();
    }

    private void createView() {
        // LEFT

        ipConverterBtn = Controls.iconButton(Material2OutlinedAL.CALCULATE);
        ipConverterBtn.setMaxHeight(Double.MAX_VALUE);
        ipConverterBtn.setTooltip(new Tooltip(t(IPCALC_IP_ADDRESS_CONVERTER)));
        ipConverterBtn.setOnAction(e -> showIPConverter());

        ipText = Controls.create(TextField::new, "monospace");
        ipText.setPrefWidth(200);
        ipText.setTextFormatter(TextFormatters.ipv4Decimal());

        HBox ipBox = hbox(0, CENTER_LEFT, Insets.EMPTY);
        ipBox.getChildren().setAll(ipConverterBtn, ipText);

        netmaskChoice = new ComboBox<>(observableArrayList(NETMASKS));
        netmaskChoice.setPrefWidth(200);
        netmaskChoice.setButtonCell(new NetmaskCell());
        netmaskChoice.setCellFactory(property -> new NetmaskCell());

        GridPane ipGrid = gridPane(10, 5, Insets.EMPTY);
        ipGrid.add(new Label(t(IP_ADDRESS)), 0, 0);
        ipGrid.add(ipBox, 0, 1);

        ipGrid.add(new Label(), 1, 0);
        ipGrid.add(netmaskChoice, 1, 1);

        Label detailsLabel = new Label(t(DETAILS));
        detailsLabel.setPadding(new Insets(5, 0, 0, 0));

        detailsTable = createDetailsTable();
        VBox.setVgrow(detailsTable, Priority.ALWAYS);

        VBox leftBox = vbox(5, CENTER_LEFT, new Insets(0, 5, 0, 0));
        leftBox.getChildren().setAll(ipGrid, detailsLabel, detailsTable);

        // RIGHT

        netmaskTable = createNetmaskTable();
        netmaskTable.setItems(observableArrayList(NETMASKS));
        netmaskTable.setMinWidth(300);

        bitUsageLabel = new BitUsageLabel();

        splitSubnetsChoice = new ComboBox<>();
        splitSubnetsChoice.setMinWidth(100);
        splitSubnetsChoice.setButtonCell(new SplitSelectorCell());
        splitSubnetsChoice.setCellFactory(property -> new SplitSelectorCell());

        HBox splitBox = hbox(10, Pos.CENTER_LEFT, new Insets(20, 0, 0, 0));
        splitBox.getChildren().setAll(
                new Label(t(IPCALC_SPLIT_TO_0)),
                splitSubnetsChoice,
                new Label(t(IPCALC_SPLIT_TO_1)),
                horizontalSpacer(),
                bitUsageLabel
        );

        splitTable = createSplitTable();
        VBox.setVgrow(splitTable, Priority.ALWAYS);

        splitExportBtn = new Button(t(ACTION_EXPORT));
        splitExportBtn.setOnAction(e -> exportSplitData());

        HBox exportBox = hbox(0, Pos.CENTER_LEFT, new Insets(0, 0, 2, 0));
        exportBox.getChildren().setAll(horizontalSpacer(), splitExportBtn);

        VBox rightBox = vbox(5, CENTER_LEFT, new Insets(0, 0, 0, 5));
        rightBox.getChildren().setAll(
                new Label(t(NETMASK)),
                netmaskTable,
                splitBox,
                splitTable,
                exportBox
        );

        // ROOT

        setDividerPositions(0.3);
        setOrientation(Orientation.HORIZONTAL);
        getItems().setAll(leftBox, rightBox);
        setPadding(new Insets(10));
        setId("ipv4-calc");
    }

    @Override
    public void initialize() {
        Component.propagateMouseEventsToParent(this);

        ipText.textProperty().bindBidirectional(model.ipAddressProperty());
        netmaskChoice.valueProperty().bindBidirectional(model.netmaskProperty());

        // sync selected table row with selected netmask
        netmaskChoice.getSelectionModel().selectedIndexProperty().addListener((obs, old, value) -> {
            if (value == null) { return; }
            netmaskTable.scrollTo(value.intValue() - 2);
            netmaskTable.getSelectionModel().select(value.intValue());
        });

        detailsTable.rootProperty().bind(Bindings.createObjectBinding(() -> {
            IPv4AddressInfo ipInfo = model.ipAddressInfoProperty().get();
            IPv4NetworkInfo netInfo = model.ipNetworkInfoProperty().get();
            if (ipInfo != null && netInfo != null) {
                return ipInfo(ipInfo, netInfo);
            } else {
                return null;
            }
        }, model.ipAddressInfoProperty(), model.ipNetworkInfoProperty()));

        model.ipNetworkInfoProperty().addListener((obs, old, value) -> {
            if (value != null) {
                splitSubnetsChoice.getItems().setAll(value.getSplitVariants());
                splitSubnetsChoice.getSelectionModel().selectFirst();
            } else {
                splitSubnetsChoice.getItems().clear();
            }
        });

        splitSubnetsChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            if (value != null) {
                IPv4NetworkInfo netInfo = model.ipNetworkInfoProperty().get();

                bitUsageLabel.setValue(netInfo.getBitUsage(value.subnetBitCount()));

                List<IPv4NetworkInfo> networks = netInfo.split(value.subnetBitCount());
                splitTable.setItems(observableArrayList(networks));

                splitExportBtn.setDisable(false);
            } else {
                bitUsageLabel.setValue(null);
                splitTable.getItems().clear();
                splitExportBtn.setDisable(true);
            }
        });

        // set initial data
        ipText.setText(DEFAULT_IP);
        netmaskChoice.getSelectionModel().select(32 - DEFAULT_NETMASK);
    }

    private TreeTableView<Pair<String, String>> createDetailsTable() {
        TreeTableColumn<Pair<String, String>, String> col0 = new TreeTableColumn<>(t(PARAMETER));
        col0.setCellValueFactory(new TreeItemPropertyValueFactory<>("key"));
        col0.setMinWidth(150);

        TreeTableColumn<Pair<String, String>, String> col1 = new TreeTableColumn<>(t(VALUE));
        col1.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));

        TreeTableView<Pair<String, String>> table = Controls.create(TreeTableView::new);
        table.setShowRoot(false);
        table.setMinWidth(300);
        table.getColumns().addAll(List.of(col0, col1));
        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                TreeItem<Pair<String, String>> item = table.getSelectionModel().getSelectedItem();
                if (item != null && isNotBlank(item.getValue().getRight())) {
                    DesktopUtils.putToClipboard(item.getValue().getRight());
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        table.setContextMenu(contextMenu);
        contextMenu.getItems().add(
                menuItem(t(ACTION_COPY_ALL), null, e -> copyDetailsTableToClipboard())
        );

        return table;
    }

    private void copyDetailsTableToClipboard() {
        List<Pair<String, String>> items = TreeUtils.getAllItems(detailsTable.getRoot());

        StringBuilder sb = new StringBuilder();
        for (Pair<String, String> item : items) {
            if (item == null) { continue; }
            sb.append(item.getLeft()).append("\t");
            sb.append(item.getRight()).append("\n");
        }
        DesktopUtils.putToClipboard(sb.toString());
    }

    private TableView<IPv4NetworkInfo> createSplitTable() {
        TableColumn<IPv4NetworkInfo, Integer> indexColumn = new TableColumn<>("#");
        setColumnConstraints(indexColumn, 100, USE_COMPUTED_SIZE, false, CENTER);
        indexColumn.setCellFactory(Tables.indexCellFactory());

        TableColumn<IPv4NetworkInfo, String> netAddressColumn = Tables.column("Network", "networkAddress");
        setColumnConstraints(netAddressColumn, 150, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableColumn<IPv4NetworkInfo, String> minHostColumn = Tables.column("Min Host", "minHost");
        setColumnConstraints(minHostColumn, 160, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableColumn<IPv4NetworkInfo, String> maxHostColumn = Tables.column("Max Host", "maxHost");
        setColumnConstraints(maxHostColumn, 160, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableColumn<IPv4NetworkInfo, String> broadcastColumn = Tables.column("Broadcast", "broadcast");
        setColumnConstraints(broadcastColumn, 150, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableView<IPv4NetworkInfo> table = new TableView<>();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getColumns().setAll(List.of(
                indexColumn, netAddressColumn, minHostColumn, maxHostColumn, broadcastColumn
        ));

        // COPY DATA

        Function<IPv4NetworkInfo, String> rowToString = net -> Stream.of(
                net.getNetworkAddress(),
                net.getMinHost(),
                net.getMaxHost(),
                net.getBroadcast()
        ).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("\t"));

        ContextMenu contextMenu = new ContextMenu();
        table.setContextMenu(contextMenu);
        contextMenu.getItems().add(
                menuItem(t(ACTION_COPY), null, e -> Tables.copySelectedRowsToClipboard(table, rowToString))
        );

        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                Tables.copySelectedRowsToClipboard(table, rowToString);
            }
        });

        return table;
    }

    private TableView<IPv4NetworkInfo> createNetmaskTable() {
        TableColumn<IPv4NetworkInfo, Integer> prefixLengthColumn = Tables.column("Prefix Length", "prefixLength");
        setColumnConstraints(prefixLengthColumn, 100, USE_COMPUTED_SIZE, false, CENTER);

        TableColumn<IPv4NetworkInfo, String> decimalValueColumn = Tables.column("Decimal", "netmaskAsDecimal");
        setColumnConstraints(decimalValueColumn, 150, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableColumn<IPv4NetworkInfo, String> hexValueColumn = Tables.column("Hex", "netmaskAsHex");
        setColumnConstraints(hexValueColumn, 150, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableColumn<IPv4NetworkInfo, String> numberOfHostsColumn = Tables.column("Total Hosts", "totalHostCountFormatted");
        setColumnConstraints(numberOfHostsColumn, 150, USE_COMPUTED_SIZE, false, CENTER);

        TableColumn<IPv4NetworkInfo, String> wildcardColumn = Tables.column("Wildcard Mask", "wildcardMask");
        setColumnConstraints(wildcardColumn, 150, USE_COMPUTED_SIZE, false, CENTER_LEFT);

        TableView<IPv4NetworkInfo> table = new TableView<>();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getColumns().setAll(List.of(
                prefixLengthColumn, decimalValueColumn, hexValueColumn, numberOfHostsColumn, wildcardColumn
        ));

        // COPY DATA

        Function<IPv4NetworkInfo, String> rowToString = net -> Stream.of(
                net.getNetmaskAsDecimal(),
                net.getNetmaskAsHex(),
                net.getTotalHostCountFormatted(),
                net.getWildcardMask()
        ).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("\t"));

        ContextMenu contextMenu = new ContextMenu();
        table.setContextMenu(contextMenu);
        contextMenu.getItems().add(
                menuItem(t(ACTION_COPY), null, e -> Tables.copySelectedRowsToClipboard(table, rowToString))
        );

        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                Tables.copySelectedRowsToClipboard(table, rowToString);
            }
        });

        return table;
    }

    @SuppressWarnings("unchecked")
    private TreeItem<Pair<String, String>> ipInfo(IPv4AddressInfo ipInfo, IPv4NetworkInfo netInfo) {
        // NETWORK

        TreeItem<Pair<String, String>> networkDetailsItem = new TreeItem<>(ImmutablePair.of(t(NETWORK), ""));
        networkDetailsItem.setExpanded(true);

        networkDetailsItem.getChildren().setAll(
                treeItem("Address", netInfo.getNetworkAddress()),
                treeItem("Netmask", netInfo.getNetmaskAsDecimal()),
                treeItem("", netInfo.getNetmaskAsBinary()),
                treeItem("", netInfo.getNetmaskAsHex()),
                treeItem("Prefix Length", String.valueOf(netInfo.getPrefixLength())),
                treeItem("Class", netInfo.getNetworkClass()),
                treeItem("Min Host", netInfo.getMinHost()),
                treeItem("Max Host", netInfo.getMaxHost()),
                treeItem("Total Hosts", NUMBER_FORMAT.format(netInfo.getTotalHostCount())),
                treeItem("Usable Hosts", NUMBER_FORMAT.format(netInfo.getUsableHostCount())),
                treeItem("Broadcast", netInfo.getBroadcast()),
                treeItem("Wildcard Mask", netInfo.getWildcardMask())
        );

        List<String> additionalInfo = netInfo.getAdditionalInfo();
        if (!additionalInfo.isEmpty()) {
            networkDetailsItem.getChildren().add(treeItem(t(COMMENT), additionalInfo.get(0)));
            if (additionalInfo.size() > 1) {
                additionalInfo.forEach(remark -> networkDetailsItem.getChildren().add(treeItem("", remark)));
            }
        }

        // ADDRESS

        TreeItem<Pair<String, String>> addressDetailsItem = new TreeItem<>(ImmutablePair.of(t(IP_ADDRESS), ""));
        addressDetailsItem.setExpanded(true);
        addressDetailsItem.getChildren().setAll(
                treeItem("Integer", ipInfo.getDecimalString()),
                treeItem("Binary", ipInfo.getBinaryString()),
                treeItem("Hex", ipInfo.getHexString()),
                treeItem("IPv4 Mapped Address", ipInfo.getIPv4MappedAddress()),
                treeItem("Reverse DNS Hostname", ipInfo.getReverseDNSLookupString())
        );

        // ROOT

        TreeItem<Pair<String, String>> detailsRoot = new TreeItem<>();
        detailsRoot.setExpanded(true);
        detailsRoot.getChildren().setAll(List.of(networkDetailsItem, addressDetailsItem));

        return detailsRoot;
    }

    private TreeItem<Pair<String, String>> treeItem(String key, String value) {
        return new TreeItem<>(ImmutablePair.of(key, value));
    }

    private void showIPConverter() {
        converterDialog = getOrCreateConverterDialog();
        converterDialog.setData(model.ipAddressProperty().get());

        overlay.show(converterDialog);
    }

    private IPv4ConverterDialog getOrCreateConverterDialog() {
        if (converterDialog != null) { return converterDialog; }

        converterDialog = new IPv4ConverterDialog();
        converterDialog.setOnCommit(ipStr -> {
            if (IPv4AddressWrapper.isValidString(ipStr)) { ipText.setText(ipStr); }
            overlay.hide();
        });
        converterDialog.setOnCloseRequest(overlay::hide);

        return converterDialog;
    }

    private void exportSplitData() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(lastVisitedDirectory)
                .initialFileName(FileUtils.sanitizeFileName("subnets.txt"))
                .build()
                .showSaveDialog(getWindow());
        if (outputFile == null || splitTable.getItems().isEmpty()) { return; }

        lastVisitedDirectory = getParentPath(outputFile);
        Promise.runAsync(() -> {
            try (FileOutputStream fos = new FileOutputStream(outputFile);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
                 BufferedWriter out = new BufferedWriter(osw)) {

                out.write("Network Address;Start Host;End Host;Broadcast\n");

                for (IPv4NetworkInfo net : splitTable.getItems()) {
                    out.write(net.getNetworkAddress());
                    out.write(";");
                    out.write(net.getMinHost());
                    out.write(";");
                    out.write(net.getMaxHost());
                    out.write(";");
                    out.write(net.getBroadcast());
                    out.write("\n");
                }
            } catch (Exception e) {
                throw new TelekitException(t(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
            }
        }).start(threadPool);
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public IPv4CalcViewModel getViewModel() { return model; }

    ///////////////////////////////////////////////////////////////////////////

    static class NetmaskCell extends ListCell<IPv4NetworkInfo> {

        @Override
        protected void updateItem(IPv4NetworkInfo net, boolean empty) {
            super.updateItem(net, empty);

            if (net != null) {
                setText(net.getNetmaskAsDecimal() + "/" + net.getPrefixLength());
            } else {
                setText(null);
            }
        }
    }

    static class SplitSelectorCell extends ListCell<SplitVariant> {

        @Override
        protected void updateItem(SplitVariant variant, boolean empty) {
            super.updateItem(variant, empty);

            if (variant != null) {
                setText(
                        NUMBER_FORMAT.format(variant.numberOfSubnets()) +
                                " x " +
                                NUMBER_FORMAT.format(variant.numberOfHosts())
                );
            } else {
                setText(null);
            }
        }
    }

    static class BitUsageLabel extends HBox {

        Label addressBitsText;
        Label subnetBitsText;
        Label hostBitsText;

        public BitUsageLabel() {
            Label helpIcon = new Label();
            helpIcon.setGraphic(Controls.fontIcon(Material2OutlinedAL.HELP_OUTLINE));
            helpIcon.setTooltip(new Tooltip("bit usage: S - subnet, H - host"));
            helpIcon.setPadding(new Insets(0, 5, 0, 0));

            addressBitsText = Controls.create(Label::new);
            subnetBitsText = Controls.create(Label::new, "text-success");
            hostBitsText = Controls.create(Label::new, "text-error");

            setAlignment(CENTER_LEFT);
            setPadding(new Insets(5, 0, 5, 0));
            getChildren().setAll(helpIcon, addressBitsText, subnetBitsText, hostBitsText);
        }

        public void setValue(IPv4NetworkInfo.BitUsage bitUsage) {
            if (bitUsage == null) {
                addressBitsText.setText("");
                subnetBitsText.setText("");
                hostBitsText.setText("");
                setVisible(false);
                return;
            }

            int subnetBitsStart = -1;
            int hostBitsStart = -1;

            char[] bits = bitUsage.toString().toCharArray();
            for (int i = 0; i < bits.length; i++) {
                if (subnetBitsStart < 0 && bits[i] == SUBNET_CHAR) { subnetBitsStart = i; }
                if (hostBitsStart < 0 && bits[i] == HOST_CHAR) { hostBitsStart = i; }
            }

            if (!isVisible()) { setVisible(true); }

            if (subnetBitsStart < 0 & hostBitsStart < 0) {
                addressBitsText.setText(bitUsage.hostAddress());
                subnetBitsText.setText("");
                hostBitsText.setText("");
            } else {
                addressBitsText.setText(new String(Arrays.copyOfRange(bits, 0, subnetBitsStart)));
                subnetBitsText.setText(new String(Arrays.copyOfRange(bits, subnetBitsStart, hostBitsStart)));
                hostBitsText.setText(new String(Arrays.copyOfRange(bits, hostBitsStart, bits.length)));
            }
        }
    }
}
