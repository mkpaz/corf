package org.telekit.controls.components.dialogs;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.telekit.base.ui.Controller;

public final class Dialogs {

    public static AlertBuilder info() {
        return new AlertBuilder(AlertType.INFORMATION);
    }

    public static AlertBuilder error() {
        return new AlertBuilder(AlertType.ERROR);
    }

    public static AlertBuilder warning() {
        return new AlertBuilder(AlertType.WARNING);
    }

    public static AlertBuilder confirm() {
        return new AlertBuilder(AlertType.CONFIRMATION);
    }

    public static FileChooserBuilder fileChooser() {
        return new FileChooserBuilder();
    }

    public static ModalBuilder modal(Parent root, Window owner) {
        return new ModalBuilder(root, owner, true);
    }

    public static ModalBuilder modal(Parent root, Window owner, boolean inheritStyles) {
        return new ModalBuilder(root, owner, inheritStyles);
    }

    public static void show(Controller controller) {
        if (controller != null) show(controller.getWindow());
    }

    public static void show(Stage stage) {
        if (stage != null && !stage.isShowing()) {
            stage.show();
        }
    }

    public static void showAndWait(Controller controller) {
        Platform.runLater(() -> {
            if (controller != null) showAndWait(controller.getWindow());
        });
    }

    public static void showAndWait(Stage stage) {
        Platform.runLater(() -> {
            if (stage != null && !stage.isShowing()) stage.showAndWait();
        });
    }

    public static void hide(Controller controller) {
        if (controller != null) hide(controller.getWindow());
    }

    public static void hide(Stage stage) {
        Platform.runLater(() -> {
            if (stage != null && stage.isShowing()) stage.hide();
        });
    }
}
