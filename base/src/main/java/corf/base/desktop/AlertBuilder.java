package corf.base.desktop;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class AlertBuilder {

    private final Alert alert;

    public AlertBuilder(AlertType type) {
        Objects.requireNonNull(type, "type");
        alert = new Alert(type);
        alert.setHeaderText(null);
        alert.getDialogPane().setMaxWidth(500);
        alert.getDialogPane().setMaxHeight(500);
    }

    public AlertBuilder title(@Nullable String title) {
        alert.setTitle(title);
        return this;
    }

    public AlertBuilder header(@Nullable String header) {
        alert.setHeaderText(header);
        return this;
    }

    public AlertBuilder content(@Nullable String content) {
        alert.setContentText(content);
        return this;
    }

    public AlertBuilder icon(@Nullable Image icon) {
        if (icon != null) {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(icon);
        }
        return this;
    }

    public AlertBuilder setButtonTypes(ButtonType... buttonTypes) {
        alert.getButtonTypes().setAll(buttonTypes);
        return this;
    }

    public AlertBuilder owner(Window owner) {
        Objects.requireNonNull(owner, "owner");
        alert.initOwner(owner);
        return this;
    }

    public Alert build() {
        return alert;
    }
}
