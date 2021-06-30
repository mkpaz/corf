package org.telekit.base.desktop;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.mvvm.View;

import static org.telekit.base.i18n.I18n.t;

/** Basic interface for any UI component */
public interface Component {

    /** Returns root element of the component */
    Region getRoot();

    /**
     * Resets the component to its default state.
     * MVVM views should delegate this to model.
     */
    void reset();

    default @Nullable Window getWindow() {
        return (getRoot() != null && getRoot().getScene() != null) ? getRoot().getScene().getWindow() : null;
    }

    default StringBinding createI18nBinding(ObservableStringValue prop) {
        return Bindings.createStringBinding(() -> prop != null && prop.get() != null ? t(prop.get()) : null, prop);
    }

    /**
     * Some JavaFX components, namely TabPane, SplitPane, ScrollPane etc, which essentially
     * are containers, implemented like controls and therefore extend {@link SkinBase} class.
     * By default, all descendants of {@link SkinBase} do not propagate mouse events to their
     * parent.
     * <p>
     * That's an issue, because since Telekit follows CSD design, it can't delegate window
     * resize to OS. The cursor have to change its shape from normal to resize and vise versa
     * somewhere near window borders. There's relatively small gap for this.
     * <p>
     * But when the {@link View} root, that consumes all mouse events (see above), is too close to
     * window borders, cursor type changing may not happen. In that case we should manually propagate
     * mouse events to the view parent. That's what this method is meant to do.
     */
    static void propagateMouseEventsToParent(Control control) {
        control.addEventHandler(MouseEvent.ANY, e -> {
            e.consume();
            control.getParent().fireEvent(e);
        });
    }
}
