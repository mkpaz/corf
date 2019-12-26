package org.telekit.base.desktop;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import org.jetbrains.annotations.Nullable;

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
}
