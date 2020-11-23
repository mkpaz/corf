package org.telekit.base.ui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.telekit.controls.domain.Dimension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.base.ui.UIDefaults.DIALOG_MAX_SIZE;

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
            System.out.println(2);
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

    ///////////////////////////////////////////////////////////////////////////

    public static class AlertBuilder {

        private final Alert alert;

        public AlertBuilder(AlertType type) {
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
            return addFilter(new ExtensionFilter(description, extensions));
        }

        public FileChooserBuilder initialDirectory(Path path) {
            if (Files.isDirectory(path)) {
                fileChooser.setInitialDirectory(path.toFile());
            }
            return this;
        }

        public FileChooserBuilder initialFileName(String filename) {
            fileChooser.setInitialFileName(filename);
            return this;
        }

        public FileChooser build() {
            return fileChooser;
        }
    }

    public static class ModalBuilder {

        private final Stage stage;
        private final Scene scene;

        public ModalBuilder(Parent parent, Window owner, boolean inheritStyles) {
            stage = new Stage();
            scene = new Scene(parent);

            stage.setScene(scene);
            // TODO: return before release
            //stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);

            if (inheritStyles && owner.getScene() != null) {
                scene.getStylesheets().addAll(owner.getScene().getStylesheets());
            }
            parent.getStyleClass().add("application-modal");
        }

        public ModalBuilder title(String title) {
            stage.setTitle(title);
            return this;
        }

        public ModalBuilder icon(Image icon) {
            stage.getIcons().add(icon);
            return this;
        }

        public ModalBuilder preferredSize(Dimension dimension) {
            Pane root = (Pane) scene.getRoot();
            root.setPrefWidth(dimension.getWidth());
            root.setPrefHeight(dimension.getHeight());
            return this;
        }

        public ModalBuilder maxSize(Dimension dimension) {
            Pane root = (Pane) scene.getRoot();
            root.setMaxWidth(dimension.getWidth());
            root.setMaxHeight(dimension.getHeight());
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
