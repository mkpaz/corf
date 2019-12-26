package org.telekit.base.fx;

import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.UUID;

public abstract class Controller {

    protected String id = UUID.randomUUID().toString();
    protected Parent parent;
    protected Stage stage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public abstract void reset();
}
