package corf.desktop.tools.httpsender;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Tweaks;
import javafx.beans.binding.Bindings;
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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.Env;
import corf.base.common.Lazy;
import corf.base.desktop.Dialogs;
import corf.base.desktop.Observables;
import corf.base.desktop.controls.FXHelpers;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static atlantafx.base.theme.Styles.*;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static corf.base.i18n.I18n.t;
import static corf.base.desktop.ExtraStyles.BG_DEFAULT;

final class LogTab extends VBox {

    private static final Function<LogRecord, String> TABLE_ROW_CONVERTER = LogTab::logRecordToString;

    HBox progressCountBox;
    Label successCountLabel;
    Label failedCountLabel;

    TableView<LogRecord> logTable;
    Lazy<LogRecordDialog> logRecordDialog;
    CheckBox errorsOnlyCheck;
    Button exportBtn;

    private final HttpSenderView view;
    private final HttpSenderViewModel model;

    public LogTab(HttpSenderView view) {
        this.view = view;
        this.model = view.getViewModel();

        this.logRecordDialog = new Lazy<>(() -> {
            var dialog = new LogRecordDialog(model);
            dialog.setOnCloseRequest(view::hideOverlay);
            return dialog;
        });

        createView();
        init();
    }

    private void createView() {
        // == HEADER ==

        var successIcon = new FontIcon(Material2OutlinedAL.CHECK_CIRCLE);
        successIcon.getStyleClass().add(SUCCESS);

        successCountLabel = new Label();
        successCountLabel.getStyleClass().add(SUCCESS);
        successCountLabel.setGraphic(successIcon);

        var failedIcon = new FontIcon(Material2OutlinedMZ.REPORT_PROBLEM);
        failedIcon.getStyleClass().add(DANGER);

        failedCountLabel = new Label();
        failedCountLabel.getStyleClass().add(DANGER);
        failedCountLabel.setGraphic(failedIcon);

        progressCountBox = new HBox(5, successCountLabel, failedCountLabel);
        progressCountBox.setVisible(false);

        var headerLabel = new Label(t(DM.HTTP_SENDER_COMPLETED_REQUESTS));
        headerLabel.getStyleClass().addAll(TEXT_BOLD, TEXT_MUTED);

        var headerBox = new HBox(5);
        headerBox.getChildren().setAll(headerLabel, new Spacer(), progressCountBox);

        // == TABLE ==

        logTable = createLogTable();
        logTable.setItems(model.getFilteredLog());
        VBox.setVgrow(logTable, Priority.ALWAYS);

        // == OPTIONS ==

        errorsOnlyCheck = new CheckBox(t(DM.HTTP_SENDER_SHOW_UNSUCCESSFUL_REQUESTS_ONLY));

        exportBtn = new Button(t(DM.ACTION_EXPORT));
        exportBtn.getStyleClass().add(FLAT);

        var optionsBox = new HBox();
        optionsBox.setPadding(new Insets(Recommends.SUB_ITEM_MARGIN, 0, 0, 0));
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        optionsBox.getChildren().addAll(errorsOnlyCheck, new Spacer(), exportBtn);

        // ~

        getStyleClass().add(BG_DEFAULT);
        getChildren().setAll(
                headerBox,
                new Spacer(Recommends.CAPTION_MARGIN, Orientation.VERTICAL),
                logTable,
                optionsBox);
    }

    private void init() {
        model.logStatProperty().addListener((obs, old, val) -> {
            if (val == null || val.isZero()) {
                progressCountBox.setVisible(false);
                successCountLabel.setText("0");
                failedCountLabel.setText("0");
                return;
            }

            if (!progressCountBox.isVisible()) { progressCountBox.setVisible(true); }

            successCountLabel.setText(String.valueOf(val.success()));
            failedCountLabel.setText(String.valueOf(val.failed()));
        });

        errorsOnlyCheck.selectedProperty().bindBidirectional(model.logErrorsOnlyProperty());

        exportBtn.setOnAction(e -> exportLog());
        exportBtn.disableProperty().bind(Observables.or(
                Bindings.isEmpty(model.getFullLog()),
                model.ongoingProperty()
        ));
    }

    private TableView<LogRecord> createLogTable() {
        var indexColumn = new TableColumn<LogRecord, Integer>("#");
        indexColumn.setCellValueFactory(new PropertyValueFactory<>("processedRange"));
        indexColumn.getStyleClass().add(Tweaks.ALIGN_CENTER);
        indexColumn.setMinWidth(75);
        indexColumn.setMaxWidth(200);

        var statusColumn = new TableColumn<LogRecord, String>(t(DM.STATUS));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusCode"));
        statusColumn.getStyleClass().add(Tweaks.ALIGN_CENTER);
        statusColumn.setMinWidth(75);
        statusColumn.setMaxWidth(200);

        var dataColumn = new TableColumn<LogRecord, String>("URL");
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("uri"));

        var table = new TableView<LogRecord>();
        table.getStyleClass().addAll("log-table", DENSE);
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getColumns().setAll(List.of(indexColumn, statusColumn, dataColumn));
        table.setRowFactory(t -> {
            var row = new LogTableRow();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showRecordDialog(row.getIndex());
                }
            });
            return row;
        });
        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                copyTableRowsToClipboard();
            }
        });

        return table;
    }

    private void exportLog() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(Env.getLastVisitedDir())
                .initialFileName(HttpSenderViewModel.LOG_FILE_NAME)
                .build()
                .showSaveDialog(view.getWindow());

        if (outputFile == null) { return; }

        model.exportLogCommand().execute(outputFile);
    }

    private void showRecordDialog(int index) {
        var dialog = logRecordDialog.get();
        dialog.setRowIndex(index);
        view.showOverlay(dialog);
    }

    private void copyTableRowsToClipboard() {
        FXHelpers.copySelectedRowsToClipboard(logTable, TABLE_ROW_CONVERTER);
    }

    private static String logRecordToString(LogRecord rec) {
        return Stream.of(rec.getProcessedRange(), String.valueOf(rec.getStatusCode()), rec.getUri())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(";"));
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class LogTableRow extends TableRow<LogRecord> {

        @Override
        protected void updateItem(LogRecord request, boolean empty) {
            super.updateItem(request, empty);
            pseudoClassStateChanged(STATE_DANGER, !empty && (request.failed() || !request.responded()));
        }
    }
}
