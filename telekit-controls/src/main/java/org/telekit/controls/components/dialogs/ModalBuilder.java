package org.telekit.controls.components.dialogs;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.telekit.base.ui.Dimension;

public class ModalBuilder {

    private final Stage stage;
    private final Scene scene;

    public ModalBuilder(Parent parent, Window owner, boolean inheritStyles) {
        stage = new Stage();
        scene = new Scene(parent);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
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