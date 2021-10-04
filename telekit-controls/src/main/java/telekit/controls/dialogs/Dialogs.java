package telekit.controls.dialogs;

import javafx.scene.control.Alert.AlertType;

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
