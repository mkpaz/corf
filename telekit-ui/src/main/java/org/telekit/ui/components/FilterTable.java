package org.telekit.ui.components;

import fontawesomefx.fa.FontAwesomeIcon;
import fontawesomefx.fa.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.NotImplementedException;
import org.telekit.base.event.CancelEvent;
import org.telekit.base.event.SubmitEvent;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.controls.i18n.ControlsMessageKeys.CANCEL;
import static org.telekit.controls.i18n.ControlsMessageKeys.SUBMIT;
import static org.telekit.controls.util.ControlUtils.addStyleClass;
import static org.telekit.controls.util.ControlUtils.removeStyleClass;

public class FilterTable<S> extends Controller {

    public TextField tfFilter;
    public Button btnFilter;
    public TableView<S> tblData;
    public Button btnSubmit;
    public Button btnCancel;

    private final ObservableList<S> rows = FXCollections.observableArrayList();
    private final FilteredList<S> filteredRows = new FilteredList<>(rows);
    private BiPredicate<String, S> predicate = (filter, row) -> String.valueOf(row).contains(filter);

    public FilterTable() {
        setView(createView());
    }

    public void initialize() {
        btnSubmit.setOnAction(event -> submit());
        btnCancel.setOnAction(event -> cancel());
    }

    private Pane createView() {
        // filter area
        tfFilter = new TextField();
        tfFilter.setAlignment(Pos.CENTER);
        tfFilter.setPrefWidth(Control.USE_COMPUTED_SIZE);
        tfFilter.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // filter table on ENTER key press
            if (event.getCode().equals(KeyCode.ENTER)) {
                filter();
                event.consume();
            }
        });
        btnFilter = new Button();
        btnFilter.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.SEARCH));
        btnFilter.setOnAction(event -> filter());
        btnFilter.getStyleClass().add("btn-icon");

        HBox paneFilter = new HBox();
        paneFilter.setSpacing(0);
        paneFilter.setAlignment(Pos.CENTER_LEFT);
        paneFilter.getChildren().setAll(tfFilter, btnFilter);
        HBox.setHgrow(tfFilter, Priority.ALWAYS);

        // table area
        tblData = new TableView<>(filteredRows);
        tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblData.setOnMouseClicked((MouseEvent event) -> {
            // submit selection on row double click
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                submit();
            }
        });

        // control area
        btnSubmit = new Button(Messages.get(SUBMIT));
        btnSubmit.setDefaultButton(true);
        btnCancel = new Button(Messages.get(CANCEL));

        HBox paneControl = new HBox();
        paneControl.setSpacing(10);
        paneControl.setPadding(new Insets(5, 0, 0, 0));
        paneControl.setAlignment(Pos.CENTER_RIGHT);
        paneControl.getChildren().setAll(btnSubmit, btnCancel);

        // root
        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(10));
        rootPane.setSpacing(10);
        rootPane.setPrefWidth(600);
        rootPane.setPrefHeight(400);
        rootPane.getChildren().addAll(
                paneFilter,
                tblData,
                paneControl
        );
        return rootPane;
    }

    public void setColumns(List<TableColumn<S, String>> columns) {
        Objects.requireNonNull(columns);
        tblData.getColumns().setAll(columns);
        if (columns.size() == 1) {
            addStyleClass(tblData, "hide-table-header");
        } else {
            removeStyleClass(tblData, "hide-table-header");
        }
    }

    @SafeVarargs
    public final void setColumns(TableColumn<S, String>... columns) {
        setColumns(List.of(columns));
    }

    public List<S> getData() {
        return new ArrayList<>(rows);
    }

    public void setData(List<S> data) {
        Objects.requireNonNull(data);
        rows.setAll(data);
        if (!data.isEmpty()) tblData.getSelectionModel().selectFirst();
    }

    public void setPredicate(BiPredicate<String, S> predicate) {
        this.predicate = Objects.requireNonNull(predicate);
    }

    public void setPredicates(Map<String, BiPredicate<String, S>> predicates) {
        // This is API draft that will allow user to switch between several filters.
        // Map key is a filter name and predicate is corresponding filter expression.
        throw new NotImplementedException();
    }

    protected void filter() {
        String filter = tfFilter.getText();
        if (isBlank(filter)) {
            filteredRows.setPredicate(row -> true);
        } else {
            filteredRows.setPredicate(row -> predicate.test(filter, row));
        }
    }

    protected void submit() {
        S data = tblData.getSelectionModel().getSelectedItem();
        eventBus.publish(new SubmitEvent<>(data));
    }

    protected void cancel() {
        eventBus.publish(new CancelEvent());
    }

    public static <S> FilterTable<S> create() {
        FilterTable<S> controller = new FilterTable<>();
        controller.initialize();
        return controller;
    }
}
