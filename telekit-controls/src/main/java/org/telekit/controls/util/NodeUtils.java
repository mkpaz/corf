package org.telekit.controls.util;

import javafx.application.Platform;
import javafx.scene.Node;

public final class NodeUtils {

    /**
     * One can't simply get focus in JavaFX <code>:boromir.jpg</code>.
     * This method will try to request it multiple times.
     */
    public static void begForFocus(Node node, int attempts) {
        if (attempts < 0) { return; }
        Platform.runLater(() -> {
            if (!node.isFocused()) {
                node.requestFocus();
                begForFocus(node, attempts - 1);
            }
        });
    }

    public static void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
