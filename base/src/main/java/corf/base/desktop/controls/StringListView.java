package corf.base.desktop.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import corf.base.desktop.OS;
import corf.base.i18n.M;

import java.util.List;

import static corf.base.i18n.I18n.t;

/**
 * {@link TextArea} has terrible performance, because it's not virtualized.
 * Starting from ~500 lines it consumes a lot of memory. This widget is one
 * of the approaches to work around the issue.
 */
public class StringListView extends ListView<String> {

    protected final ReadOnlyIntegerWrapper size = new ReadOnlyIntegerWrapper();

    protected MenuItem copyMenuItem;
    protected MenuItem selectAllMenuItem;
    protected ContextMenu contextMenu;

    @SuppressWarnings("NullAway.Init")
    public StringListView() {
        setContextMenu(createContextMenu());
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
        copyMenuItem = new MenuItem(t(M.ACTION_COPY));
        selectAllMenuItem = new MenuItem(t(M.ACTION_SELECT_ALL));

        contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(
                copyMenuItem,
                new SeparatorMenuItem(),
                selectAllMenuItem
        );

        return contextMenu;
    }

    protected void init() {
        copyMenuItem.setOnAction(e -> copySelectedRowsToClipboard());
        copyMenuItem.disableProperty().bind(size.isEqualTo(0));

        selectAllMenuItem.setOnAction(e -> getSelectionModel().selectAll());
        selectAllMenuItem.disableProperty().bind(size.isEqualTo(0));
    }

    public int getSize() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size.getReadOnlyProperty();
    }

    private void copySelectedRowsToClipboard() {
        List<String> selectedItems = getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) { return; }
        OS.setClipboard(String.join("\n", selectedItems));
    }
}
