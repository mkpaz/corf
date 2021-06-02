package org.telekit.base.desktop;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

public class ModalDialog<T> {

    private final Stage stage;
    private final Scene scene;
    private final Region root;
    private final T component;

    public ModalDialog(Region region, T component, Window owner) {
        Objects.requireNonNull(region);
        Objects.requireNonNull(owner);

        this.stage = new Stage();
        this.scene = new Scene(region);
        this.root = region;
        this.component = component;

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
    }

    public Stage getStage() { return stage; }

    public Scene getScene() { return scene; }

    public Region getRoot() { return root; }

    public T getComponent() { return component; }

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

    public static <T extends Component> Builder<T> builder(T component, Window owner) {
        return builder(component.getRoot(), component, owner);
    }

    public static <T> Builder<T> builder(Region region, T controller, Window owner) {
        return new Builder<>(region, controller, owner);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Builder<T> {

        private final ModalDialog<T> dialog;
        private final Window owner;

        public Builder(Region region, T component, Window owner) {
            this.dialog = new ModalDialog<>(region, component, owner);
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
