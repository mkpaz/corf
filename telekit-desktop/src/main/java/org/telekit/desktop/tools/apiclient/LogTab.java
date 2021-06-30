package org.telekit.desktop.tools.apiclient;

import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.telekit.base.desktop.Component;
import org.telekit.base.util.FileUtils;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.controls.util.Tables;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.fontIcon;
import static org.telekit.controls.util.Tables.setColumnConstraints;
import static org.telekit.desktop.i18n.DesktopMessages.*;

public class LogTab extends Tab {

    HBox statBox;
    Label successCount;
    Label failedCount;

    TableView<CompletedRequest> logTable;
    TextArea details;
    CheckBox errorsOnlyCheckBox;
    Button exportBtn;

    private final ApiClientView view;
    private final ApiClientViewModel model;

    public LogTab(ApiClientView view) {
        this.view = view;
        this.model = view.getViewModel();

        setText(t(LOG));

        createView();
    }

    private void createView() {
        // LEFT

        successCount = Controls.create(Label::new);
        failedCount = Controls.create(Label::new);

        statBox = hbox(5, CENTER_LEFT, Insets.EMPTY);
        statBox.getChildren().setAll(
                fontIcon(Material2OutlinedMZ.VERIFIED, "text-success"),
                successCount,
                fontIcon(Material2OutlinedMZ.REPORT, "text-error"),
                failedCount
        );
        statBox.setVisible(false);

        model.logStatProperty().addListener((obs, old, value) -> {
            if (value == null || value.isEmpty()) {
                statBox.setVisible(false);
                successCount.setText("0");
                failedCount.setText("0");
                return;
            }

            if (!statBox.isVisible()) { statBox.setVisible(true); }

            successCount.setText(String.valueOf(value.successCount()));
            failedCount.setText(String.valueOf(value.failedCount()));
        });

        HBox leftTopBox = hbox(5, CENTER_LEFT, Insets.EMPTY);
        leftTopBox.getChildren().setAll(
                new Label(t(APICLIENT_COMPLETED_REQUESTS)),
                horizontalSpacer(),
                statBox
        );

        logTable = createLogTable();
        logTable.setItems(model.getFilteredLog());
        VBox.setVgrow(logTable, Priority.ALWAYS);

        errorsOnlyCheckBox = new CheckBox(t(APICLIENT_SHOW_UNSUCCESSFUL_REQUESTS_ONLY));
        errorsOnlyCheckBox.selectedProperty().bindBidirectional(model.logErrorsOnlyProperty());

        exportBtn = new Button(t(ACTION_EXPORT));
        exportBtn.setOnAction(e -> exportLog());
        exportBtn.disableProperty().bind(BindUtils.or(
                Bindings.isEmpty(model.getFullLog()),
                model.ongoingProperty()
        ));

        HBox optsBox = hbox(5, CENTER_LEFT, new Insets(0, 0, 5, 0));
        Containers.setFixedHeight(optsBox, 30);
        optsBox.getChildren().addAll(
                errorsOnlyCheckBox,
                horizontalSpacer(),
                exportBtn
        );

        VBox leftBox = vbox(5, CENTER_LEFT, new Insets(0, 2, 0, 0));
        leftBox.getChildren().addAll(
                leftTopBox,
                logTable,
                optsBox
        );

        // RIGHT

        details = Controls.create(TextArea::new, "monospace");
        VBox.setVgrow(details, Priority.ALWAYS);

        VBox rightBox = vbox(5, CENTER_LEFT, new Insets(0, 0, optsBox.getPrefHeight() + leftBox.getSpacing(), 2));
        rightBox.getChildren().addAll(
                new Label(t(DETAILS)),
                details
        );

        // ROOT

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.7);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().setAll(leftBox, rightBox);
        splitPane.setPadding(new Insets(10));

        Component.propagateMouseEventsToParent(splitPane);

        setContent(splitPane);
    }

    private TableView<CompletedRequest> createLogTable() {
        TableColumn<CompletedRequest, Integer> indexColumn = Tables.column("#", "processedRange");
        setColumnConstraints(indexColumn, 70, 70, false, Pos.CENTER);

        TableColumn<CompletedRequest, String> statusColumn = Tables.column(t(STATUS), "statusCode");
        setColumnConstraints(statusColumn, 100, USE_COMPUTED_SIZE, false, Pos.CENTER);

        TableColumn<CompletedRequest, String> dataColumn = Tables.column(t(APICLIENT_REQUEST_LINE), "userData");
        Tables.setColumnConstraints(dataColumn, 200, USE_COMPUTED_SIZE, true, CENTER_LEFT);

        TableView<CompletedRequest> table = new TableView<>();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        table.getColumns().setAll(List.of(indexColumn, statusColumn, dataColumn));
        table.setRowFactory(t -> new LogTableRow());
        table.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                Tables.copySelectedRowsToClipboard(table, row -> Stream.of(
                        row.getProcessedRange(),
                        String.valueOf(row.getStatusCode()),
                        row.getUserData()
                ).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining(" | ")));
            }
        });

        TableViewSelectionModel<CompletedRequest> selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        selectionModel.selectedItemProperty().addListener((obs, old, value) -> displayRequestDetails(value));

        return table;
    }

    private void displayRequestDetails(CompletedRequest request) {
        if (request != null) { details.setText(request.print()); }
    }

    private void exportLog() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_TEXT), "*.txt")
                .initialFileName(FileUtils.sanitizeFileName("api-client-log.txt"))
                .build()
                .showSaveDialog(view.getWindow());

        if (outputFile != null) { model.exportLogCommand().execute(outputFile); }
    }

    ///////////////////////////////////////////////////////////////////////////

    static class LogTableRow extends TableRow<CompletedRequest> {

        private static final PseudoClass REQUEST_FAILED = PseudoClass.getPseudoClass("failed");
        private static final PseudoClass REQUEST_FORWARDED = PseudoClass.getPseudoClass("forwarded");

        @Override
        protected void updateItem(CompletedRequest request, boolean empty) {
            super.updateItem(request, empty);
            pseudoClassStateChanged(REQUEST_FAILED, !empty && (request.isFailed() || !request.isResponded()));
            pseudoClassStateChanged(REQUEST_FORWARDED, !empty && request.isForwarded());
        }
    }
}
