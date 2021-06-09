package org.telekit.controls.widgets;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.NotImplementedException;
import org.telekit.base.i18n.I18n;
import org.telekit.controls.glyphs.FontAwesome;
import org.telekit.controls.glyphs.FontAwesomeIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.controls.i18n.ControlsMessages.ACTION_CANCEL;
import static org.telekit.controls.i18n.ControlsMessages.ACTION_SUBMIT;

public class FilterTable<S> extends VBox {

    protected TextField tfFilter;
    protected Button btnFilter;
    protected TableView<S> tblData;
    protected Button btnSubmit;
    protected Button btnCancel;

    protected final ObservableList<S> rows = FXCollections.observableArrayList();
    protected final FilteredList<S> filteredRows = new FilteredList<>(rows);
    protected BiPredicate<String, S> predicate = (filter, row) -> String.valueOf(row).contains(filter);
    protected Consumer<S> onSubmitCallback;
    protected Runnable onCancelCallback;

    public FilterTable() {
        createView();
    }

    private void createView() {
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
        btnFilter.setGraphic(new FontAwesomeIcon(FontAwesome.SEARCH));
        btnFilter.setOnAction(event -> filter());
        btnFilter.getStyleClass().add("link-button");

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
        btnSubmit = new Button(I18n.t(ACTION_SUBMIT));
        btnSubmit.setDefaultButton(true);
        btnSubmit.setOnAction(event -> {
            if (onSubmitCallback != null) { onSubmitCallback.accept(submit()); }
        });

        btnCancel = new Button(I18n.t(ACTION_CANCEL));
        btnCancel.setOnAction(event -> {
            if (onCancelCallback != null) { onCancelCallback.run(); }
        });

        HBox paneControl = new HBox();
        paneControl.setSpacing(10);
        paneControl.setPadding(new Insets(5, 0, 0, 0));
        paneControl.setAlignment(Pos.CENTER_RIGHT);
        paneControl.getChildren().setAll(btnSubmit, btnCancel);

        setPadding(new Insets(10));
        setSpacing(10);
        setPrefWidth(600);
        setPrefHeight(400);
        getChildren().addAll(paneFilter, tblData, paneControl);
    }

    public void setColumns(List<TableColumn<S, String>> columns) {
        Objects.requireNonNull(columns);
        tblData.getColumns().setAll(columns);
        if (columns.size() == 1) {
            tblData.getStyleClass().add("hide-table-header");
        } else {
            tblData.getStyleClass().remove("hide-table-header");
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

    public void setOnSubmit(Consumer<S> handler) {
        this.onSubmitCallback = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancelCallback = handler;
    }

    protected void filter() {
        String filter = tfFilter.getText();
        if (isBlank(filter)) {
            filteredRows.setPredicate(row -> true);
        } else {
            filteredRows.setPredicate(row -> predicate.test(filter, row));
        }
    }

    protected S submit() {
        return tblData.getSelectionModel().getSelectedItem();
    }
}
