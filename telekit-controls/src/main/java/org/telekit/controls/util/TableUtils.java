package org.telekit.controls.util;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public final class TableUtils {

    public static <S, T> TableColumn<S, T> createTableColumn(String text, String propertyName) {
        TableColumn<S, T> column = new TableColumn<>(text);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> createIndexCellFactory() {
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
}
