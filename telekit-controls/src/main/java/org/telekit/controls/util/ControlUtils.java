package org.telekit.controls.util;

import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Collection;
import java.util.Set;

public class ControlUtils {

    public static <S, T> TableColumn<S, T> createTableColumn(String text, String propertyName) {
        TableColumn<S, T> column = new TableColumn<>(text);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    public static void addStyleClass(Node node, Collection<String> styleClasses) {
        if (node == null || styleClasses == null || styleClasses.isEmpty()) return;
        node.getStyleClass().addAll(styleClasses);
    }

    public static void addStyleClass(Node node, String... styleClasses) {
        addStyleClass(node, Set.of(styleClasses));
    }

    public static void removeStyleClass(Node node, Collection<String> styleClasses) {
        if (node == null || styleClasses == null || styleClasses.isEmpty()) return;
        node.getStyleClass().removeIf(styleClasses::contains);
    }

    public static void removeStyleClass(Node node, String... styleClasses) {
        removeStyleClass(node, Set.of(styleClasses));
    }
}
