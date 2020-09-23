package org.telekit.base.fx;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.telekit.base.IconCache;

import static org.telekit.base.IconCache.ICON_APP;

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

    public static FileChooserBuilder file() {
        return new FileChooserBuilder();
    }

    public static ModalBuilder modal(Parent root) {
        return new ModalBuilder(root);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class AlertBuilder {

        private final Alert alert;

        public AlertBuilder(AlertType type) {
            this.alert = new Alert(type);
            this.alert.setHeaderText(null);

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
            ((Stage) this.alert.getDialogPane().getScene().getWindow()).getIcons().add(icon);
            return this;
        }

        public AlertBuilder setButtonTypes(ButtonType... buttonTypes) {
            alert.getButtonTypes().setAll(buttonTypes);
            return this;
        }

        public AlertBuilder owner(Stage owner) {
            alert.initOwner(owner);
            return this;
        }

        public Alert build() {
            return alert;
        }
    }

    public static class FileChooserBuilder {

        private final FileChooser fileChooser;

        public FileChooserBuilder() {
            this.fileChooser = new FileChooser();
        }

        public FileChooserBuilder addFilter(ExtensionFilter filter) {
            fileChooser.getExtensionFilters().add(filter);
            return this;
        }

        public FileChooserBuilder addFilter(String description, String... extensions) {
            fileChooser.getExtensionFilters().add(new ExtensionFilter(description, extensions));
            return this;
        }

        public FileChooserBuilder initialDirectory(String path) {
            fileChooser.setInitialFileName(path);
            return this;
        }

        public FileChooserBuilder initialFilename(String filename) {
            fileChooser.setInitialFileName(filename);
            return this;
        }

        public FileChooser build() {
            return fileChooser;
        }
    }

    public static class ModalBuilder {

        private final Stage stage;

        public ModalBuilder(Parent root) {
            stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
        }

        public ModalBuilder title(String title) {
            stage.setTitle(title);
            return this;
        }

        public ModalBuilder icon(Image icon) {
            stage.getIcons().add(icon);
            return this;
        }

        public ModalBuilder owner(Window owner) {
            stage.initOwner(owner);
            return this;
        }

        public ModalBuilder resizable(boolean resizable) {
            stage.setResizable(resizable);
            return this;
        }

        public Stage build() {
            return stage;
        }
    }
}
