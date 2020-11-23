package org.telekit.controls.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;

public final class BindUtils {

    public static void rebind(BooleanProperty property, ObservableValue<? extends Boolean> observable) {
        property.unbind();
        property.bind(observable);
    }
}
