package org.telekit.controls.widgets;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material2.Material2AL;
import org.telekit.base.desktop.ModalController;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;

import java.util.Objects;

import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.ACTION_CLOSE;
import static org.telekit.controls.util.Containers.horizontalSpacer;
import static org.telekit.controls.util.Controls.button;

public abstract class OverlayDialog extends VBox implements ModalController {

    protected Label titleLabel;
    protected Button topCloseBtn;
    protected HBox headerBox;

    protected Button bottomCloseBtn;
    protected HBox footerBox;

    protected Runnable onCloseCallback;

    public OverlayDialog() {
        createView();
    }

    protected void createView() {
        titleLabel = Controls.create(Label::new, "title");

        topCloseBtn = Controls.circleIconButton(Material2AL.CLOSE, "close-button");
        topCloseBtn.setOnAction(e -> close());

        headerBox = Containers.hbox(10, Pos.CENTER_LEFT, Insets.EMPTY, "header");
        headerBox.getChildren().setAll(
                titleLabel,
                horizontalSpacer(),
                topCloseBtn
        );
        VBox.setVgrow(headerBox, Priority.NEVER);

        bottomCloseBtn = button(t(ACTION_CLOSE), null, "form-action");
        bottomCloseBtn.setOnAction(e -> close());
        bottomCloseBtn.setCancelButton(true);

        footerBox = Containers.hbox(10, Pos.CENTER_RIGHT, Insets.EMPTY, "footer");
        footerBox.getChildren().setAll(
                horizontalSpacer(),
                bottomCloseBtn
        );
        VBox.setVgrow(footerBox, Priority.NEVER);

        // do not call createContent() until header and footer container initialization
        // so, they can be modified in descendants
        Region content = Objects.requireNonNull(createContent());
        VBox.setVgrow(content, Priority.ALWAYS);

        // IMPORTANT: this guarantees client will use correct width and height
        Containers.usePrefWidth(this);
        Containers.usePrefHeight(this);

        getChildren().setAll(headerBox, content, footerBox);
        getStyleClass().add("overlay-dialog");
    }

    protected abstract Region createContent();

    protected void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void close() { if (onCloseCallback != null) { onCloseCallback.run(); } }

    @Override
    public Runnable getOnCloseRequest() { return onCloseCallback; }

    @Override
    public void setOnCloseRequest(Runnable handler) { this.onCloseCallback = handler; }
}
