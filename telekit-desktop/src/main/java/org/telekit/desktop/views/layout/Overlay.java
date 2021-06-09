package org.telekit.desktop.views.layout;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.di.Initializable;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.controls.util.NodeUtils;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.function.Consumer;

import static org.telekit.controls.util.Containers.setAnchors;
import static org.telekit.controls.util.Containers.setScrollConstraints;

@Singleton
public class Overlay extends StackPane implements Initializable {

    ScrollPane scrollPane;
    AnchorPane edgeContentWrapper;
    StackPane centerContentWrapper;

    private final ReadOnlyBooleanWrapper onFrontProperty = new ReadOnlyBooleanWrapper(this, "onFront", false);
    private HPos currentContentPos;

    public Overlay() {
        createView();
    }

    private void createView() {
        edgeContentWrapper = Containers.create(AnchorPane::new, "scrollable-content");

        centerContentWrapper = Containers.create(StackPane::new, "scrollable-content");
        centerContentWrapper.setPadding(new Insets(100, 0, 100, 0));

        scrollPane = Controls.create(ScrollPane::new, "edge-to-edge");
        setScrollConstraints(scrollPane,
                ScrollPane.ScrollBarPolicy.AS_NEEDED, true,
                ScrollPane.ScrollBarPolicy.NEVER, true
        );
        scrollPane.setMaxHeight(20000); // scroll pane won't work without height specified

        getChildren().add(scrollPane);
        getStyleClass().add("overlay");
    }

    @Override
    public void initialize() {
        Consumer<Event> hideAndConsume = e -> {
            toBack();
            e.consume();
        };

        // hide overlay by pressing ESC (only works when overlay or on of its children has focus,
        // that's why we requesting it in the toFront())
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) { hideAndConsume.accept(e); }
        });

        // hide overlay by clicking outside content area
        setOnMouseClicked(e -> {
            Pane content = getContent();
            Point2D coordinates = new Point2D(e.getX(), e.getY());
            if (e.getButton() == MouseButton.PRIMARY && content != null && !content.contains(coordinates)) {
                hideAndConsume.accept(e);
            }
        });
    }

    public @Nullable Pane getContent() {
        return NodeUtils.getChildByIndex(getContentWrapper(), 0, Pane.class);
    }

    private @NotNull Pane getContentWrapper() {
        return currentContentPos == HPos.CENTER ? centerContentWrapper : edgeContentWrapper;
    }

    public void setContent(Pane content, HPos pos) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(pos);

        // clear old content
        if (pos != currentContentPos) {
            removeContent();
            currentContentPos = pos;
        }

        switch (pos) {
            case LEFT -> {
                edgeContentWrapper.getChildren().setAll(content);
                setAnchors(content, new Insets(0, -1, 0, 0));
            }
            case RIGHT -> {
                edgeContentWrapper.getChildren().setAll(content);
                setAnchors(content, new Insets(0, 0, 0, -1));
            }
            case CENTER -> {
                centerContentWrapper.getChildren().setAll(content);
                StackPane.setAlignment(content, Pos.TOP_CENTER);
            }
        }
        scrollPane.setContent(getContentWrapper());
    }

    public void removeContent() {
        getContentWrapper().getChildren().clear();
    }

    public boolean contains(Pane content) {
        return content != null &&
                getContentWrapper().getChildren().size() > 0 &&
                getContentWrapper().getChildren().get(0).equals(content);
    }

    @Override
    public void toFront() {
        if (onFrontProperty.get()) { return; }
        super.toFront();
        onFrontProperty.set(true);
        requestFocus();
    }

    @Override
    public void toBack() {
        if (!onFrontProperty.get()) { return; }
        super.toBack();
        onFrontProperty.set(false);
    }

    public ReadOnlyBooleanProperty onFrontProperty() {
        return onFrontProperty.getReadOnlyProperty();
    }

    public boolean isOnFront() {
        // this is foolproof method, but return value is not observable
        // TODO: refactor to viewOrderProperty?
        // if (getParent() == null) { return false; }
        // ObservableList<Node> siblings = getParent().getChildrenUnmodifiable();
        // return !siblings.isEmpty() && siblings.get(siblings.size() - 1).equals(this);

        // this may return incorrect value if toBack() or toFront() was called on sibling node
        // so, only call those methods on Overlay
        return onFrontProperty().get();
    }
}
