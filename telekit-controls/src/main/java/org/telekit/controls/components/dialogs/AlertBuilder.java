package org.telekit.controls.components.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.telekit.base.ui.IconCache;

import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.base.ui.UIDefaults.DIALOG_MAX_SIZE;

public class AlertBuilder {

    private final Alert alert;

    public AlertBuilder(Alert.AlertType type) {
        this.alert = new Alert(type);
        this.alert.setHeaderText(null);
        this.alert.getDialogPane().setMaxWidth(DIALOG_MAX_SIZE.getWidth());
        this.alert.getDialogPane().setMaxHeight(DIALOG_MAX_SIZE.getHeight());

        Image appIcon = IconCache.get(ICON_APP);
        if (appIcon != null) icon(appIcon);
    }

    public AlertBuilder title(String title) {
        alert.setTitle(title);
        return this;
    }

    public AlertBuilder header(String header) {
        alert.setHeaderText(header);
        return this;
    }

    public AlertBuilder content(String content) {
        alert.setContentText(content);
        return this;
    }

    public AlertBuilder icon(Image icon) {
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(icon);
        return this;
    }

    public AlertBuilder setButtonTypes(ButtonType... buttonTypes) {
        alert.getButtonTypes().setAll(buttonTypes);
        return this;
    }

    public AlertBuilder owner(Window owner) {
        alert.initOwner(owner);
        return this;
    }

    public Alert build() {
        return alert;
    }
}