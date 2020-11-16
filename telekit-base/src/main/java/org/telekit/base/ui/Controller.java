package org.telekit.base.ui;

import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.UUID;

public abstract class Controller {

    // used to identify the source of the events, notifications or tasks
    protected final String id = UUID.randomUUID().toString();

    // parent node (root pane) for this controller
    protected Parent parent;

    // primary or modal window stage
    protected Stage stage;

    public String getId() {
        return id;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Used to initialize controls after controller construction.
     * If method will be automatically called by FXMLLoader.
     */
    public void initialize() {}

    /**
     * Resets all controls to their default state.
     */
    public void reset() {}
}
