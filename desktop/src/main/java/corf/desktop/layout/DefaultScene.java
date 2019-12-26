package corf.desktop.layout;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.desktop.EventID;

public final class DefaultScene extends Scene {

    public DefaultScene(double width, double height) {
        super(new AnchorPane(), width, height);
        registerGlobalHotkeys();
    }

    private void registerGlobalHotkeys() {
        setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_ANY).match(e)) {
                handleEvent(e, new ActionEvent<>(EventID.APP_SHOW_NAVIGATION));
            }
            if (new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_ANY).match(e)) {
                handleEvent(e, new ActionEvent<>(EventID.TOOL_CREATE_NEW_TAB));
            }
            if (new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_ANY).match(e)) {
                handleEvent(e, new ActionEvent<>(EventID.TOOL_CLOSE_CURRENT_TAB));
            }
        });
    }

    private void handleEvent(KeyEvent ke, ActionEvent<?> ae) {
        Events.fire(ae);
        ke.consume();
    }
}
