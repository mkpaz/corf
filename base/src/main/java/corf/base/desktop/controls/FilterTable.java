package corf.base.desktop.controls;

import atlantafx.base.theme.Styles;
import corf.base.desktop.ExtraStyles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class FilterTable<S> extends VBox {

    protected TextField filterText;
    protected Button filterBtn;
    protected HBox filterBox;
    protected TableView<S> dataTable;

    protected final ObservableList<S> items = FXCollections.observableArrayList();
    protected final FilteredList<S> filteredItems = new FilteredList<>(items);
    protected BiPredicate<String, S> predicate = (filter, item) -> String.valueOf(item).contains(filter);

    @SuppressWarnings("NullAway.Init")
    public FilterTable() {
        super();

        createView();
        init();
    }

    protected void createView() {
        filterText = new TextField();
        filterText.getStyleClass().add(Styles.LEFT_PILL);
        filterText.setAlignment(Pos.CENTER);
        HBox.setHgrow(filterText, Priority.ALWAYS);

        filterBtn = new Button("", new FontIcon(Material2MZ.SEARCH));
        filterBtn.getStyleClass().add(Styles.RIGHT_PILL);
        filterBtn.setCursor(Cursor.HAND);

        filterBox = new HBox(filterText, filterBtn);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        dataTable = new TableView<>(filteredItems);
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setSpacing(10);
        getChildren().addAll(filterBox, dataTable);
    }

    protected void init() {
        filterText.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                applyFilter();
                event.consume();
            }
        });

        filterBtn.setOnAction(event -> applyFilter());
    }

    @SafeVarargs
    public final void setColumns(TableColumn<S, String>... columns) {
        setColumns(List.of(columns));
    }

    public void setColumns(List<TableColumn<S, String>> columns) {
        Objects.requireNonNull(columns, "columns");
        dataTable.getColumns().setAll(columns);
        if (columns.size() == 1) {
            dataTable.getStyleClass().add(ExtraStyles.NO_TABLE_HEADER);
        } else {
            dataTable.getStyleClass().remove(ExtraStyles.NO_TABLE_HEADER);
        }
    }

    public List<S> getData() {
        return new ArrayList<>(items);
    }

    public void setData(@Nullable List<S> data) {
        items.setAll(Objects.requireNonNullElse(data, new ArrayList<>()));
        if (!items.isEmpty()) {
            dataTable.getSelectionModel().selectFirst();
        }
    }

    public void setPredicate(BiPredicate<String, S> predicate) {
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    public TableView<S> getDataTable() {
        return dataTable;
    }

    public S getSelectedItem() {
        return dataTable.getSelectionModel().getSelectedItem();
    }

    public void resetFilter() {
        filterText.setText(null);
        applyFilter();
    }

    protected void applyFilter() {
        var filter = filterText.getText();
        if (StringUtils.isBlank(filter)) {
            filteredItems.setPredicate(item -> true);
        } else {
            filteredItems.setPredicate(item -> predicate.test(filter, item));
        }
    }
}
