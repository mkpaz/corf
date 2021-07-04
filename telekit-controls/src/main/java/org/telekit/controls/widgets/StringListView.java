package org.telekit.controls.widgets;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.telekit.base.util.DesktopUtils;

import java.util.List;

import static javafx.collections.FXCollections.emptyObservableList;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.*;

public class StringListView extends ListView<String> {

    private final ReadOnlyIntegerWrapper size = new ReadOnlyIntegerWrapper(this, "size");

    MenuItem clearItem;

    public StringListView() {
        setContextMenu(createContextMenu());

        size.bind(Bindings.createIntegerBinding(() -> {
            List<String> items = itemsProperty().get();
            return items != null ? items.size() : 0;
        }, itemsProperty()));

        setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                copySelectedRowsToClipboard();
            }
        });

        getStyleClass().add("string-list-view");
    }

    private ContextMenu createContextMenu() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem copyItem = new MenuItem(t(ACTION_COPY));
        copyItem.setOnAction(e -> copySelectedRowsToClipboard());
        copyItem.disableProperty().bind(size.isEqualTo(0));

        clearItem = new MenuItem(t(ACTION_CLEAR));
        clearItem.setOnAction(e -> setItems(emptyObservableList()));
        clearItem.disableProperty().bind(size.isEqualTo(0));

        MenuItem selectAllItem = new MenuItem(t(ACTION_SELECT_ALL));
        selectAllItem.setOnAction(e -> getSelectionModel().selectAll());
        selectAllItem.disableProperty().bind(size.isEqualTo(0));

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(
                copyItem,
                clearItem,
                new SeparatorMenuItem(),
                selectAllItem
        );

        return contextMenu;
    }

    public int getSize() { return size.get(); }

    public ReadOnlyIntegerProperty sizeProperty() { return size.getReadOnlyProperty(); }

    public boolean isClearable() { return clearItem.isVisible(); }

    public void setClearable(boolean state) { clearItem.setVisible(state); }

    private void copySelectedRowsToClipboard() {
        List<String> selectedItems = getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) { return; }
        DesktopUtils.putToClipboard(String.join("\n", selectedItems));
    }
}
