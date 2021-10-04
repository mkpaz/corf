package telekit.base.desktop;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

public class ModalDialog<T extends ModalController> {

    private final Stage stage;
    private final Scene scene;
    private final Region root;
    private final T controller;

    public ModalDialog(Region region, T controller, Window owner) {
        Objects.requireNonNull(region);
        Objects.requireNonNull(owner);

        this.stage = new Stage();
        this.scene = new Scene(region);
        this.root = region;
        this.controller = controller;

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
    }

    public Stage getStage() { return stage; }

    public Scene getScene() { return scene; }

    public Region getRoot() { return root; }

    public T getController() { return controller; }

    public void show() {
        Platform.runLater(() -> {
            if (!stage.isShowing()) {
                root.getStyleClass().add("application-modal");
                stage.show();
            }
        });
    }

    public void showAndWait() {
        Platform.runLater(() -> {
            if (!stage.isShowing()) {
                root.getStyleClass().add("application-modal");
                stage.showAndWait();
            }
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            if (stage.isShowing()) {
                root.getStyleClass().remove("application-modal");
                stage.hide();
            }
        });
    }

    public static <T extends Component & ModalController> Builder<T> builder(T controller, Window owner) {
        return builder(controller.getRoot(), controller, owner);
    }

    public static <T extends ModalController> Builder<T> builder(Region region, T controller, Window owner) {
        return new Builder<>(region, controller, owner);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Builder<T extends ModalController> {

        private final ModalDialog<T> dialog;
        private final Window owner;

        public Builder(Region region, T controller, Window owner) {
            this.dialog = new ModalDialog<>(region, controller, owner);
            this.owner = owner;
        }

        public Builder<T> title(String title) {
            dialog.getStage().setTitle(title);
            return this;
        }

        public Builder<T> icon(Image icon) {
            dialog.getStage().getIcons().add(icon);
            return this;
        }

        public Builder<T> prefSize(Dimension dimension) {
            dialog.getRoot().setPrefWidth(dimension.width());
            dialog.getRoot().setPrefHeight(dimension.height());
            return this;
        }

        public Builder<T> maxSize(Dimension dimension) {
            dialog.getRoot().setMaxWidth(dimension.width());
            dialog.getRoot().setMaxHeight(dimension.height());
            return this;
        }

        public Builder<T> inheritStyles() {
            dialog.getScene().getStylesheets().addAll(owner.getScene().getStylesheets());
            return this;
        }

        public Builder<T> resizable(boolean resizable) {
            dialog.getStage().setResizable(resizable);
            return this;
        }

        public ModalDialog<T> build() {
            return dialog;
        }
    }
}
