package corf.base.desktop.controls;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.jetbrains.annotations.Nullable;
import corf.base.desktop.OS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class FXHelpers {

    /**
     * Attempts to request focus multiple times.
     * Because one can't simply get focus in JavaFX.
     */
    public static void begForFocus(Node node, int attempts) {
        Objects.requireNonNull(node, "node");
        if (attempts < 0) { return; }

        Platform.runLater(() -> {
            if (!node.isFocused()) {
                node.requestFocus();
                begForFocus(node, attempts - 1);
            }
        });
    }

    /** Toggles node transparency and managed state. */
    public static void setManaged(Node node, boolean on) {
        Objects.requireNonNull(node, "node");
        node.setVisible(on);
        node.setManaged(on);
    }

    public static @Nullable <T> T findChildByIndex(Parent parent,
                                                   int index,
                                                   Class<T> nodeClass) {
        var children = parent.getChildrenUnmodifiable();
        if (index < 0 || index >= children.size()) { return null; }
        var node = children.get(index);
        return nodeClass.isInstance(node) ? nodeClass.cast(node) : null;
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

    public static <S> void copySelectedRowsToClipboard(TableView<S> table, Function<S, String> converter) {
        List<Integer> rowIndices = table.getSelectionModel().getSelectedIndices();
        if (rowIndices == null || rowIndices.isEmpty()) { return; }

        StringBuilder sb = new StringBuilder();
        for (Integer rowIndex : rowIndices) {
            sb.append(converter.apply(table.getItems().get(rowIndex)));
            sb.append('\n');
        }

        OS.setClipboard(sb.toString());
    }

    public static <T> List<T> collectTreeItems(TreeItem<T> item) {
        ArrayList<T> accumulator = new ArrayList<>();
        collectTreeItems(item, accumulator);
        return accumulator;
    }

    private static <T> void collectTreeItems(TreeItem<T> item, List<T> accumulator) {
        T value = item.getValue();
        if (value != null) { accumulator.add(value); }

        if (item.getChildren().size() > 0) {
            for (TreeItem<T> subItem : item.getChildren()) {
                collectTreeItems(subItem, accumulator);
            }
        }
    }
}
