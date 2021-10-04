package telekit.base.desktop;

import javafx.geometry.HPos;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

public interface Overlay {

    @Nullable Pane getContent();

    void setContent(Pane content, HPos pos);

    void removeContent();

    void toFront();

    void toBack();

    default void show(Pane content) {
        show(content, HPos.CENTER);
    }

    default void show(Pane content, HPos pos) {
        setContent(content, pos);
        toFront();
    }

    default void hide() {
        removeContent();
        toBack();
    }
}
