package org.telekit.base.desktop;

import javafx.application.Platform;
import javafx.scene.Node;
import org.jetbrains.annotations.Nullable;

public interface Focusable {

    @Nullable Node getPrimaryFocusNode();

    default void begForFocus(int attempts) {
        Node node = getPrimaryFocusNode();
        if (node == null || attempts < 0) { return; }
        Platform.runLater(() -> {
            if (!node.isFocused()) {
                node.requestFocus();
                begForFocus(attempts - 1);
            }
        });
    }
}
