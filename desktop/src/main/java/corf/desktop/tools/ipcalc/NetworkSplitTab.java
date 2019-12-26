package corf.desktop.tools.ipcalc;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Tweaks;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.Env;
import corf.base.desktop.Dialogs;
import corf.base.desktop.controls.FXHelpers;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.ipcalc.NetworkInfo.BitUsage;
import corf.desktop.tools.ipcalc.NetworkInfo.SplitOption;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

final class NetworkSplitTab extends Tab {

    private static final Function<NetworkInfo, String> TABLE_ROW_CONVERTER = NetworkSplitTab::networkInfoToString;

    ComboBox<SplitOption> splitChoice;
    BitUsageLabel bitUsageLabel;
    TableView<NetworkInfo> splitTable;
    Button exportBtn;

    private final IPv4CalcViewModel model;

    public NetworkSplitTab(IPv4CalcViewModel model) {
        super();
        this.model = model;

        createView();
        init();
    }

    private void createView() {
        splitChoice = new ComboBox<>();
        splitChoice.setMinWidth(100);
        splitChoice.setButtonCell(new SplitSelectorCell());
        splitChoice.setCellFactory(property -> new SplitSelectorCell());

        bitUsageLabel = new BitUsageLabel();

        splitTable = createSplitTable();
        splitTable.getStyleClass().addAll(DENSE);
        VBox.setVgrow(splitTable, Priority.ALWAYS);

        var splitBox = new HBox(Recommends.FORM_INLINE_SPACING);
        splitBox.setAlignment(Pos.CENTER_LEFT);
        splitBox.getChildren().setAll(
                new Label(t(DM.IPV4CALC_SPLIT_TO_0)),
                splitChoice,
                new Label(t(DM.IPV4CALC_SPLIT_TO_1)),
                new Spacer(),
                bitUsageLabel
        );

        exportBtn = new Button(t(DM.ACTION_EXPORT), new FontIcon(Material2OutlinedMZ.SAVE_ALT));
        exportBtn.getStyleClass().add(FLAT);

        var exportBox = new HBox(exportBtn);
        exportBox.setAlignment(Pos.CENTER_RIGHT);
        exportBox.setPadding(new Insets(Recommends.SUB_ITEM_MARGIN, 0, 0, 0));

        var root = new VBox();
        root.getChildren().setAll(
                splitBox,
                new Spacer(Recommends.CAPTION_MARGIN, Orientation.VERTICAL),
                splitTable,
                exportBox
        );
        root.setPadding(new Insets(Recommends.CONTENT_SPACING, 0, 0, 0));
        root.setMaxHeight(IPv4CalcView.TAB_HEIGHT);

        setText(t(DM.IPV4CALC_SPLIT_NETWORK));
        setContent(root);
    }

    private void init() {
        model.networkInfoProperty().addListener((obs, old, val) -> {
            if (val != null) {
                splitChoice.getItems().setAll(val.getSplitOptions());
                splitChoice.getSelectionModel().selectFirst();
            } else {
                splitChoice.getItems().clear();
            }
        });

        splitChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                NetworkInfo netInfo = model.networkInfoProperty().get();

                bitUsageLabel.setValue(netInfo.getBitUsage(val.subnetBitCount()));

                List<NetworkInfo> networks = netInfo.split(val.subnetBitCount());
                splitTable.setItems(FXCollections.observableArrayList(networks));

                exportBtn.setDisable(false);
            } else {
                bitUsageLabel.setValue(null);
                splitTable.getItems().clear();
                exportBtn.setDisable(true);
            }
        });

        exportBtn.setOnAction(e -> exportTableRowsToFile());
    }

    private TableView<NetworkInfo> createSplitTable() {
        var indexColumn = new TableColumn<NetworkInfo, Integer>("#");
        indexColumn.setMinWidth(100);
        indexColumn.setPrefWidth(100);
        indexColumn.getStyleClass().add(Tweaks.ALIGN_CENTER);
        indexColumn.setCellFactory(FXHelpers.createIndexCellFactory());

        var networkCol = new TableColumn<NetworkInfo, String>("Network");
        networkCol.setCellValueFactory(new PropertyValueFactory<>("networkAddress"));
        networkCol.setMinWidth(150);
        networkCol.setPrefWidth(150);

        var minHostCol = new TableColumn<NetworkInfo, String>("Min Host");
        minHostCol.setCellValueFactory(new PropertyValueFactory<>("minHost"));
        minHostCol.setMinWidth(160);
        minHostCol.setPrefWidth(160);

        var maxHostCol = new TableColumn<NetworkInfo, String>("Max Host");
        maxHostCol.setCellValueFactory(new PropertyValueFactory<>("maxHost"));
        maxHostCol.setMinWidth(160);
        maxHostCol.setPrefWidth(160);

        var broadcastCol = new TableColumn<NetworkInfo, String>("Broadcast");
        broadcastCol.setCellValueFactory(new PropertyValueFactory<>("broadcast"));
        broadcastCol.setMinWidth(150);
        broadcastCol.setPrefWidth(150);

        var table = new TableView<NetworkInfo>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getColumns().setAll(List.of(indexColumn, networkCol, minHostCol, maxHostCol, broadcastCol));
        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                copyTableRowsToClipboard();
            }
        });

        return table;
    }

    private void copyTableRowsToClipboard() {
        FXHelpers.copySelectedRowsToClipboard(splitTable, TABLE_ROW_CONVERTER);
    }

    private void exportTableRowsToFile() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(Env.getLastVisitedDir())
                .initialFileName("subnets.txt")
                .build()
                .showSaveDialog(getTabPane().getScene().getWindow());
        if (outputFile == null || splitTable.getItems().isEmpty()) { return; }

        model.exportSplitTableCommand().execute(ImmutablePair.of(outputFile, splitTable.getItems()));
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class SplitSelectorCell extends ListCell<SplitOption> {

        @Override
        protected void updateItem(SplitOption option, boolean empty) {
            super.updateItem(option, empty);

            if (option != null && !empty) {
                setText(IPv4CalcViewModel.NUMBER_FORMAT.format(option.numberOfSubnets())
                                + " x "
                                + IPv4CalcViewModel.NUMBER_FORMAT.format(option.numberOfHosts())
                );
            } else {
                setText(null);
            }
        }
    }

    private static class BitUsageLabel extends HBox {

        private final Label addressBitsText;
        private final Label subnetBitsText;
        private final Label hostBitsText;

        public BitUsageLabel() {
            var helpLabel = new Label();
            helpLabel.setGraphic(new FontIcon(Material2OutlinedAL.HELP_OUTLINE));
            helpLabel.setTooltip(new Tooltip("Bit Usage: S - subnet, X - host"));
            helpLabel.setPadding(new Insets(0, 5, 0, 0));

            addressBitsText = new Label();
            addressBitsText.getStyleClass().add(ExtraStyles.MONOSPACE);

            subnetBitsText = new Label();
            subnetBitsText.getStyleClass().addAll(ExtraStyles.MONOSPACE, SUCCESS);

            hostBitsText = new Label();
            hostBitsText.getStyleClass().addAll(ExtraStyles.MONOSPACE, ACCENT);

            setAlignment(Pos.CENTER_RIGHT);
            getChildren().setAll(helpLabel, addressBitsText, subnetBitsText, hostBitsText);
        }

        @SuppressWarnings("ShortCircuitBoolean")
        public void setValue(@Nullable BitUsage bitUsage) {
            if (bitUsage == null) {
                addressBitsText.setText(null);
                subnetBitsText.setText(null);
                hostBitsText.setText(null);
                FXHelpers.setManaged(this, false);
                return;
            }

            var bitUsageStr = bitUsage.toString();
            int subnetStart = bitUsageStr.indexOf(BitUsage.SUBNET_CHAR);
            int hostStart = bitUsageStr.indexOf(BitUsage.HOST_CHAR);

            if (subnetStart < 0 & hostStart < 0) {
                addressBitsText.setText(bitUsage.hostAddress());
                subnetBitsText.setText(null);
                hostBitsText.setText(null);
            } else {
                addressBitsText.setText(bitUsageStr.substring(0, subnetStart));
                subnetBitsText.setText(bitUsageStr.substring(subnetStart, hostStart));
                hostBitsText.setText(bitUsageStr.substring(hostStart));
            }

            FXHelpers.setManaged(this, true);
        }
    }

    private static String networkInfoToString(NetworkInfo net) {
        return Stream.of(net.getNetworkAddress(), net.getMinHost(), net.getMaxHost(), net.getBroadcast())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(";"));
    }
}
