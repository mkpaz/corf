package corf.base.desktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

public interface Overlay {

    @Nullable Pane getContent();

    void setContent(Pane content, @Nullable Pos pos, @Nullable Insets padding);

    void removeContent();

    void toFront();

    void toBack();

    default void show(Pane content) {
        show(content, Pos.CENTER);
    }

    default void show(Pane content, @Nullable Pos pos) {
        show(content, pos, Insets.EMPTY);
    }

    default void show(Pane content, @Nullable Insets padding) {
        show(content, Pos.CENTER, padding);
    }

    default void show(Pane content, @Nullable Pos pos, @Nullable Insets padding) {
        setContent(content, pos, padding);
        toFront();
    }

    default void hide() {
        removeContent();
        toBack();
    }
}
