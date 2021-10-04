package telekit.controls.widgets;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import telekit.controls.i18n.ControlsMessages;

import java.util.ResourceBundle;

public abstract class AbstractTextInputContextMenu extends ContextMenu {

    protected final ContextMenuPolicy policy;
    protected MenuItem miUndo;
    protected MenuItem miRedo;
    protected MenuItem miCut;
    protected MenuItem miCopy;
    protected MenuItem miPaste;
    protected MenuItem miDelete;
    protected MenuItem miSelectAll;

    public AbstractTextInputContextMenu() {
        this(null, null);
    }

    public AbstractTextInputContextMenu(ResourceBundle bundle, ContextMenuPolicy policy) {
        super();
        this.policy = policy != null ? policy : new ContextMenuPolicy();

        if (bundle == null) {
            createItems(
                    "Undo",
                    "Redo",
                    "Cut",
                    "Copy",
                    "Paste",
                    "Delete",
                    "Select All"
            );
            return;
        }

        createItems(
                bundle.getString(ControlsMessages.ACTION_UNDO),
                bundle.getString(ControlsMessages.ACTION_REDO),
                bundle.getString(ControlsMessages.ACTION_CUT),
                bundle.getString(ControlsMessages.ACTION_COPY),
                bundle.getString(ControlsMessages.ACTION_PASTE),
                bundle.getString(ControlsMessages.ACTION_DELETE),
                bundle.getString(ControlsMessages.ACTION_SELECT_ALL)
        );
    }

    private void createItems(String textUndo,
                             String textRedo,
                             String textCut,
                             String textCopy,
                             String textPaste,
                             String textDelete,
                             String textSelectAll) {

        miUndo = new MenuItem(textUndo);
        miRedo = new MenuItem(textRedo);
        miCut = new MenuItem(textCut);
        miCopy = new MenuItem(textCopy);
        miPaste = new MenuItem(textPaste);
        miDelete = new MenuItem(textDelete);
        miSelectAll = new MenuItem(textSelectAll);

        if (policy.isShowIcons()) {
            miUndo.setGraphic(new FontIcon(Material2MZ.UNDO));
            miRedo.setGraphic(new FontIcon(Material2MZ.REPEAT));
            miCut.setGraphic(new FontIcon(Material2AL.CONTENT_CUT));
            miCopy.setGraphic(new FontIcon(Material2AL.CONTENT_COPY));
            miPaste.setGraphic(new FontIcon(Material2AL.CONTENT_PASTE));
        }

        // item order matters
        getItems().addAll(
                miUndo,
                miRedo,
                miCut,
                miCopy,
                miPaste,
                miDelete,
                miSelectAll
        );
    }

    protected void afterContextMenuVisible() {}

    protected void onContextMenuRequested(boolean contextMenuVisible) {
        if (!contextMenuVisible) { return; }

        // initialize vars that needed to pass below checks here
        afterContextMenuVisible();

        if (policy.isHideInactiveItems()) {
            miUndo.setVisible(isEditable() && isUndoAvailable());
            miRedo.setVisible(isEditable() && isRedoAvailable());
            miCut.setVisible(isEditable() && hasSelection());
            miCopy.setVisible(hasSelection());
            miPaste.setVisible(isEditable());
            miDelete.setVisible(isEditable() && hasSelection());
        } else {
            miUndo.setDisable(!isEditable() || !isUndoAvailable());
            miRedo.setDisable(!isEditable() || !isRedoAvailable());
            miCut.setDisable(!isEditable() || !hasSelection());
            miCopy.setDisable(!hasSelection());
            miPaste.setDisable(!isEditable());
            miDelete.setDisable(!isEditable() || !hasSelection());
        }
    }

    protected abstract boolean isEditable();

    protected abstract boolean hasSelection();

    protected abstract boolean isUndoAvailable();

    protected abstract boolean isRedoAvailable();

}
