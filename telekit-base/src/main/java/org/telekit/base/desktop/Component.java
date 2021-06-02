package org.telekit.base.desktop;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import static org.telekit.base.i18n.I18n.t;

/** Basic interface for any UI component */
public interface Component {

    /** Returns root element of the component */
    Region getRoot();

    /** Resets component to its default state. MVVM views should delegate it to the model. */
    void reset();

    default Window getWindow() {
        return getRoot().getScene().getWindow();
    }

    default StringBinding createI18nBinding(ObservableStringValue prop) {
        return Bindings.createStringBinding(() -> prop != null && prop.get() != null ? t(prop.get()) : null, prop);
    }
}
