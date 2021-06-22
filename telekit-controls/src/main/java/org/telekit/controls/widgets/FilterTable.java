package org.telekit.controls.widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.controls.util.Containers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.controls.util.Controls.button;

public class FilterTable<S> extends VBox {

    protected TextField filterText;
    protected Button filterButton;
    protected TableView<S> dataTable;

    protected final ObservableList<S> rows = FXCollections.observableArrayList();
    protected final FilteredList<S> filteredRows = new FilteredList<>(rows);
    protected BiPredicate<String, S> predicate = (filter, row) -> String.valueOf(row).contains(filter);

    public FilterTable() {
        createView();
    }

    private void createView() {
        filterText = new TextField();
        filterText.setAlignment(Pos.CENTER);
        filterText.setPrefWidth(Control.USE_COMPUTED_SIZE);
        filterText.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // filter table on ENTER key press
            if (event.getCode().equals(KeyCode.ENTER)) {
                filter();
                event.consume();
            }
        });
        HBox.setHgrow(filterText, Priority.ALWAYS);

        filterButton = button("", Material2MZ.SEARCH, "link-button");
        filterButton.setOnAction(event -> filter());
        filterButton.setCursor(Cursor.HAND);

        HBox filterBox = Containers.hbox(0, Pos.CENTER_LEFT, Insets.EMPTY);
        filterBox.getChildren().setAll(filterText, filterButton);

        dataTable = new TableView<>(filteredRows);
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setSpacing(10);
        setPadding(new Insets(10));
        getChildren().addAll(filterBox, dataTable);
    }

    public void setColumns(List<TableColumn<S, String>> columns) {
        Objects.requireNonNull(columns);
        dataTable.getColumns().setAll(columns);
        if (columns.size() == 1) {
            dataTable.getStyleClass().add("hide-table-header");
        } else {
            dataTable.getStyleClass().remove("hide-table-header");
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
        if (!data.isEmpty()) { dataTable.getSelectionModel().selectFirst(); }
    }

    public void setPredicate(BiPredicate<String, S> predicate) {
        this.predicate = Objects.requireNonNull(predicate);
    }

    public TextField getFilterTextField() {
        return filterText;
    }

    public TableView<S> getDataTable() {
        return dataTable;
    }

    public S getSelectedItem() {
        return dataTable.getSelectionModel().getSelectedItem();
    }

    protected void filter() {
        String filter = filterText.getText();
        if (isBlank(filter)) {
            filteredRows.setPredicate(row -> true);
        } else {
            filteredRows.setPredicate(row -> predicate.test(filter, row));
        }
    }
}
