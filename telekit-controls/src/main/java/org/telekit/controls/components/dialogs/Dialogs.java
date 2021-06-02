package org.telekit.controls.components.dialogs;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window;

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
}
