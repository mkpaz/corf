package org.telekit.controls.util;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.telekit.base.util.DesktopUtils;

import java.util.List;
import java.util.function.Function;

public final class TableUtils {

    public static <S, T> TableColumn<S, T> createColumn(String text, String propertyName) {
        TableColumn<S, T> column = new TableColumn<>(text);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    public static <S, T> void setColumnConstraints(TableColumn<S, T> column,
                                                   double minWidth,
                                                   double prefWidth,
                                                   boolean hgrow,
                                                   Pos alignment) {
        column.setMinWidth(minWidth);
        column.setPrefWidth(prefWidth);

        if (hgrow) {
            column.setMaxWidth(Double.MAX_VALUE);
        } else {
            column.setMaxWidth(Region.USE_COMPUTED_SIZE);
        }

        if (alignment == null) { return; }

        String cssValue = String.valueOf(alignment).replace('_', '-');
        column.setStyle("-fx-alignment:" + cssValue);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> indexCellFactory() {
        return column -> {
            TableCell<S, T> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.createStringBinding(() -> {
                if (cell.isEmpty()) {
                    return null;
                } else {
                    return Integer.toString(cell.getIndex() + 1);
                }
            }, cell.emptyProperty(), cell.indexProperty()));
            return cell;
        };
    }

    public static <S> void copySelectedRowsToClipboard(TableView<S> table, Function<S, String> converter) {
        List<Integer> rowIndices = table.getSelectionModel().getSelectedIndices();
        if (rowIndices == null || rowIndices.isEmpty()) { return; }

        StringBuilder sb = new StringBuilder();
        for (Integer rowIndex : rowIndices) {
            sb.append(converter.apply(table.getItems().get(rowIndex)));
            sb.append('\n');
        }

        DesktopUtils.putToClipboard(sb.toString());
    }
}
