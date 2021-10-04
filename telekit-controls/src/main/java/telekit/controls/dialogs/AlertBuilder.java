package telekit.controls.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import telekit.base.desktop.Dimension;

public final class AlertBuilder {

    public static final Dimension MAX_SIZE = new Dimension(500, 500);

    private final Alert alert;

    public AlertBuilder(Alert.AlertType type) {
        alert = new Alert(type);
        alert.setHeaderText(null);
        alert.getDialogPane().setMaxWidth(MAX_SIZE.width());
        alert.getDialogPane().setMaxHeight(MAX_SIZE.height());
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