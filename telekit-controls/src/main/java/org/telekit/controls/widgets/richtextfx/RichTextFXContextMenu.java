package org.telekit.controls.widgets.richtextfx;

import javafx.scene.control.IndexRange;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.undo.UndoManager;
import org.telekit.controls.widgets.AbstractTextInputContextMenu;
import org.telekit.controls.widgets.ContextMenuPolicy;

import java.util.ResourceBundle;

@SuppressWarnings("rawtypes")
public class RichTextFXContextMenu extends AbstractTextInputContextMenu {

    // will be set in afterContextMenuVisible() each time context menu is shown
    private GenericStyledArea<?, ?, ?> area;
    private IndexRange range;
    private UndoManager history;

    public RichTextFXContextMenu() {
        this(null, null);
    }

    public RichTextFXContextMenu(ResourceBundle bundle, ContextMenuPolicy policy) {
        super(bundle, policy);
        initialize();
    }

    protected void initialize() {
        // init class-level variables and handle menu items state
        showingProperty().addListener((observable, oldVal, newVal) -> onContextMenuRequested(newVal));

        miUndo.setOnAction(event -> { hide(); area.undo(); });
        miRedo.setOnAction(event -> { hide(); area.redo(); });
        miCut.setOnAction(event -> { hide(); area.cut(); });
        miCopy.setOnAction(event -> { hide(); area.copy(); });
        miPaste.setOnAction(event -> { hide(); area.paste(); });
        miDelete.setOnAction(event -> { hide(); area.deleteText(range); });
        miSelectAll.setOnAction(event -> { hide(); area.selectAll(); });
    }

    protected void afterContextMenuVisible() {
        area = (GenericStyledArea) getOwnerNode();
        range = area.getSelection();
        history = area.getUndoManager();
    }

    @Override
    protected boolean isEditable() {
        return area != null && area.isEditable();
    }

    @Override
    protected boolean hasSelection() {
        return range != null && range.getLength() > 0;
    }

    @Override
    protected boolean isUndoAvailable() {
        return history != null && history.isUndoAvailable();
    }

    @Override
    protected boolean isRedoAvailable() {
        return history != null && history.isRedoAvailable();
    }
}