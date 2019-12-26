package corf.desktop.tools.ipcalc;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import corf.base.desktop.controls.FXHelpers;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static corf.base.i18n.I18n.t;

final class NetmasksTab extends Tab {

    private static final Function<NetworkInfo, String> TABLE_ROW_CONVERTER = NetmasksTab::networkInfoToString;

    TableView<NetworkInfo> netmaskTable;

    private final IPv4CalcViewModel model;

    public NetmasksTab(IPv4CalcViewModel model) {
        super();
        this.model = model;

        createView();
        init();
    }

    private void createView() {
        netmaskTable = createNetmaskTable();
        netmaskTable.getStyleClass().addAll(Styles.DENSE);
        VBox.setVgrow(netmaskTable, Priority.ALWAYS);

        var root = new VBox();
        root.getChildren().setAll(netmaskTable);
        root.setPadding(new Insets(Recommends.CONTENT_SPACING, 0, 0, 0));
        root.setMaxHeight(IPv4CalcView.TAB_HEIGHT);

        setText(t(DM.IPV4CALC_NETMASKS));
        setContent(root);
    }

    private void init() {
        netmaskTable.setItems(FXCollections.observableArrayList(IPv4CalcViewModel.NETMASKS));

        // sync selected table row with selected netmask
        model.netmaskInfoProperty().addListener((obs, old, val) -> selectNetmask());
        selectNetmask();
    }

    private TableView<NetworkInfo> createNetmaskTable() {
        var prefixCol = new TableColumn<NetworkInfo, Integer>("Prefix Length");
        prefixCol.setCellValueFactory(new PropertyValueFactory<>("prefixLength"));
        prefixCol.getStyleClass().add(Tweaks.ALIGN_CENTER);

        var decimalCol = new TableColumn<NetworkInfo, String>("Decimal");
        decimalCol.setCellValueFactory(new PropertyValueFactory<>("netmaskAsDecimal"));

        var hexCol = new TableColumn<NetworkInfo, String>("Hex");
        hexCol.setCellValueFactory(new PropertyValueFactory<>("netmaskAsHex"));

        var hostCountCol = new TableColumn<NetworkInfo, String>("Total Hosts");
        hostCountCol.setCellValueFactory(new PropertyValueFactory<>("totalHostCountFormatted"));
        hostCountCol.getStyleClass().add(Tweaks.ALIGN_CENTER);

        var wildcardCol = new TableColumn<NetworkInfo, String>("Wildcard Mask");
        wildcardCol.setCellValueFactory(new PropertyValueFactory<>("wildcardMask"));

        var table = new TableView<NetworkInfo>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getColumns().setAll(List.of(prefixCol, decimalCol, hexCol, hostCountCol, wildcardCol));
        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                copyTableRowsToClipboard();
            }
        });

        return table;
    }

    private void selectNetmask() {
        var netmask = model.netmaskInfoProperty().get();

        if (netmask == null) {
            netmaskTable.getSelectionModel().clearSelection();
            return;
        }

        var rowNum = IPv4CalcViewModel.NETMASKS.size() - netmask.getPrefixLength();
        netmaskTable.scrollTo(rowNum - 2);
        netmaskTable.getSelectionModel().clearAndSelect(rowNum);
    }

    private void copyTableRowsToClipboard() {
        FXHelpers.copySelectedRowsToClipboard(netmaskTable, TABLE_ROW_CONVERTER);
    }

    private static String networkInfoToString(NetworkInfo net) {
        return Stream.of(net.getNetmaskAsDecimal(), net.getNetmaskAsHex(),
                        net.getTotalHostCountFormatted(), net.getWildcardMask())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(";"));
    }
}
