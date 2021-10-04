package telekit.controls.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.Nullable;
import telekit.base.desktop.Focusable;

import java.util.List;

public final class NodeUtils {

    /**
     * One can't simply get focus in JavaFX <code>:boromir.jpg</code>.
     * This method will try to request it multiple times.
     */
    public static void begForFocus(@Nullable Node node, int attempts) {
        if (node == null) { return; }
        final Node target = (node instanceof Focusable focusable && focusable.getPrimaryFocusNode() != null) ?
                focusable.getPrimaryFocusNode() :
                node;
        doBegForFocus(target, attempts);
    }

    private static void doBegForFocus(Node node, int attempts) {
        if (attempts < 0) { return; }

        Platform.runLater(() -> {
            if (!node.isFocused()) {
                node.requestFocus();
                begForFocus(node, attempts - 1);
            }
        });
    }

    public static void toggleVisibility(Node node, boolean on) {
        node.setVisible(on);
        node.setManaged(on);
    }

    public static boolean isDoubleClick(MouseEvent e) {
        return e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2;
    }

    public static @Nullable <T> T getChildByIndex(Parent parent, int index, Class<T> contentType) {
        List<Node> children = parent.getChildrenUnmodifiable();
        if (index < 0 || index >= children.size()) { return null; }
        Node node = children.get(index);
        return contentType.isInstance(node) ? contentType.cast(node) : null;
    }

    public static boolean isDescendantOf(Node ancestor, Node descendant) {
        if (ancestor == null) { return true; }

        while (descendant != null) {
            if (descendant == ancestor) { return true; }
            descendant = descendant.getParent();
        }
        return false;
    }
}
