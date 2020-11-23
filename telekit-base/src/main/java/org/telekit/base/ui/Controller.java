package org.telekit.base.ui;

import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.event.EventBus;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Controller {

    protected final String id = UUID.randomUUID().toString();
    protected final AnchorPane parent = new AnchorPane();
    protected final EventBus eventBus = new DefaultEventBus();

    /**
     * Controller ID can be used to identify the source of the events, notifications or tasks.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns parent node (top-level pane).
     */
    public @NotNull Parent getParent() {
        return Objects.requireNonNull(this.parent);
    }

    /**
     * Should be executed to initialize controller state after construction.
     * This method will be automatically called by FXMLLoader.
     */
    public void initialize() {}

    /**
     * Sets a view for the controller, in other words it connects parent node to the
     * descendant hierarchy. It works this way to guarantee that parent is final and not null.
     */
    public void setView(Pane pane) {
        Objects.requireNonNull(pane);
        AnchorPane.setTopAnchor(pane, 0d);
        AnchorPane.setRightAnchor(pane, 0d);
        AnchorPane.setBottomAnchor(pane, 0d);
        AnchorPane.setLeftAnchor(pane, 0d);
        this.parent.getChildren().setAll(pane);
    }

    /**
     * Resets all controls to their default state.
     */
    public void reset() {}

    /*
     * Returns {@code Stage} that holds parent {@code Node} of the controller.
     * It will return non-null value <b>only if</b> parent node has been already added to a {@code Scene}.
     */
    public @Nullable Stage getWindow() {
        return parent.getScene() != null ? (Stage) parent.getScene().getWindow() : null;
    }

    /**
     * Creates a subscription to the controller events.
     */
    public <T> void subscribe(Class<? extends T> eventType, Consumer<T> consumer) {
        eventBus.subscribe(eventType, consumer);
    }
}
