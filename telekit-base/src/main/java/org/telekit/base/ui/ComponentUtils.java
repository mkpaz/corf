package org.telekit.base.ui;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ComponentUtils {

    public static <S, T> TableColumn<S, T> createTableColumn(String text, String propertyName) {
        TableColumn<S, T> column = new TableColumn<>(text);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }
}
