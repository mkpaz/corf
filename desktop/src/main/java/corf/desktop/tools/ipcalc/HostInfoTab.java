package corf.desktop.tools.ipcalc;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.desktop.Dialogs;
import corf.base.desktop.OS;
import corf.base.desktop.controls.FXHelpers;
import corf.base.io.FileSystemUtils;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.List;

import static atlantafx.base.theme.Styles.DENSE;
import static atlantafx.base.theme.Styles.FLAT;
import static corf.base.i18n.I18n.t;
import static corf.desktop.tools.ipcalc.IPv4CalcViewModel.NUMBER_FORMAT;

final class HostInfoTab extends Tab {

    TreeTableView<Pair<String, String>> infoTable;
    MenuButton actionsBtn;
    MenuItem copyAllItem;
    MenuItem exportHostsItem;

    private final IPv4CalcViewModel model;

    public HostInfoTab(IPv4CalcViewModel model) {
        super();
        this.model = model;

        createView();
        init();
    }

    private void createView() {
        infoTable = createDetailsTable();
        infoTable.getStyleClass().addAll(DENSE);
        VBox.setVgrow(infoTable, Priority.ALWAYS);

        copyAllItem = new MenuItem(t(DM.ACTION_COPY_ALL));
        exportHostsItem = new MenuItem(t(DM.IPV4CALC_GENERATE_IP_ADDRESSES));

        actionsBtn = new MenuButton(t(DM.ACTIONS));
        actionsBtn.getStyleClass().add(FLAT);
        actionsBtn.getItems().setAll(copyAllItem, new SeparatorMenuItem(), exportHostsItem);

        var actionsBox = new HBox(actionsBtn);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(Recommends.SUB_ITEM_MARGIN, 0, 0, 0));

        var root = new VBox(infoTable, actionsBox);
        root.setPadding(new Insets(Recommends.CONTENT_SPACING, 0, 0, 0));
        root.setMaxHeight(IPv4CalcView.TAB_HEIGHT);

        setText(t(DM.IPV4CALC_NETWORK_INFO));
        setContent(root);
    }

    private void init() {
        infoTable.rootProperty().bind(Bindings.createObjectBinding(() -> {
            var host = model.hostInfoProperty().get();
            var network = model.networkInfoProperty().get();
            if (host != null && network != null) {
                return createTreeRoot(host, network);
            } else {
                return null;
            }
        }, model.hostInfoProperty(), model.networkInfoProperty()));

        copyAllItem.setOnAction(e -> copyTreeTableContentToClipboard());

        exportHostsItem.setOnAction(e -> exportAllHostsInNetwork());
        exportHostsItem.disableProperty().bind(Bindings.not(
                model.exportAllHostsInNetworkCommand().executableProperty())
        );
    }

    private TreeTableView<Pair<String, String>> createDetailsTable() {
        var keyCol = new TreeTableColumn<Pair<String, String>, String>(t(DM.PARAMETER));
        keyCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("key"));
        keyCol.setMinWidth(250);
        keyCol.setPrefWidth(250);
        keyCol.setMaxWidth(400);

        var valueCol = new TreeTableColumn<Pair<String, String>, String>(t(DM.VALUE));
        valueCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));

        var table = new TreeTableView<Pair<String, String>>();
        table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(List.of(keyCol, valueCol));
        table.setShowRoot(false);
        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                TreeItem<Pair<String, String>> item = table.getSelectionModel().getSelectedItem();
                if (item != null && StringUtils.isNotBlank(item.getValue().getRight())) {
                    OS.setClipboard(item.getValue().getRight());
                }
            }
        });

        return table;
    }

    @SuppressWarnings("unchecked")
    private TreeItem<Pair<String, String>> createTreeRoot(HostInfo host, NetworkInfo network) {
        var networkItem = new TreeItem<Pair<String, String>>(ImmutablePair.of(t(DM.NETWORK), ""));
        networkItem.setExpanded(true);
        networkItem.getChildren().setAll(
                createTreeItem("Address", network.getNetworkAddress()),
                createTreeItem("Netmask", network.getNetmaskAsDecimal()),
                createTreeItem("", network.getNetmaskAsBinary()),
                createTreeItem("", network.getNetmaskAsHex()),
                createTreeItem("Prefix Length", String.valueOf(network.getPrefixLength())),
                createTreeItem("Class", network.getNetworkClass()),
                createTreeItem("Min Host", network.getMinHost()),
                createTreeItem("Max Host", network.getMaxHost()),
                createTreeItem("Total Hosts", NUMBER_FORMAT.format(network.getTotalHostCount())),
                createTreeItem("Usable Hosts", NUMBER_FORMAT.format(network.getUsableHostCount())),
                createTreeItem("Broadcast", network.getBroadcast()),
                createTreeItem("Wildcard Mask", network.getWildcardMask())
        );

        List<String> extraInfo = network.getExtraInfo();
        if (!extraInfo.isEmpty()) {
            networkItem.getChildren().add(createTreeItem(t(DM.COMMENT), extraInfo.get(0)));
            if (extraInfo.size() > 1) {
                extraInfo.forEach(remark -> networkItem.getChildren().add(createTreeItem("", remark)));
            }
        }

        var ipItem = new TreeItem<Pair<String, String>>(ImmutablePair.of(t(DM.IP_ADDRESS), ""));
        ipItem.setExpanded(true);
        ipItem.getChildren().setAll(
                createTreeItem("Integer", host.getDecimalString()),
                createTreeItem("Binary", host.getBinaryString()),
                createTreeItem("Hex", host.getHexString()),
                createTreeItem("IPv4 Mapped Address", host.getIPv4MappedAddress()),
                createTreeItem("Reverse DNS Hostname", host.getReverseDNSLookupString())
        );

        // == ROOT ==

        var treeRoot = new TreeItem<Pair<String, String>>();
        treeRoot.getChildren().setAll(List.of(networkItem, ipItem));
        treeRoot.setExpanded(true);

        return treeRoot;
    }

    private TreeItem<Pair<String, String>> createTreeItem(String key, @Nullable String value) {
        return new TreeItem<>(ImmutablePair.of(key, value));
    }

    private void copyTreeTableContentToClipboard() {
        List<Pair<String, String>> items = FXHelpers.collectTreeItems(infoTable.getRoot());

        var sb = new StringBuilder();
        for (var item : items) {
            if (item == null) { continue; }
            sb.append(item.getLeft()).append("\t");
            sb.append(item.getRight()).append("\n");
        }
        OS.setClipboard(sb.toString());
    }

    private void exportAllHostsInNetwork() {
        var filename = String.format(
                "%s_%s.txt",
                model.networkInfoProperty().get().getNetworkAddress(),
                model.networkInfoProperty().get().getPrefixLength()
        );
        var outputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(Env.getLastVisitedDir())
                .initialFileName(FileSystemUtils.sanitizeFileName(filename))
                .build()
                .showSaveDialog(getTabPane().getScene().getWindow());
        if (outputFile == null) { return; }

        model.exportAllHostsInNetworkCommand().execute(outputFile);
    }
}
