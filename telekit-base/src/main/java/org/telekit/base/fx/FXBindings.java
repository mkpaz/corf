package org.telekit.base.fx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

public final class FXBindings {

    public static BooleanBinding isNull(Object o) {
        return Bindings.createBooleanBinding(() -> o == null);
    }

    public static BooleanBinding isNotNull(Object o) {
        return Bindings.createBooleanBinding(() -> o != null);
    }

    public static BooleanBinding isBlank(StringProperty textProperty) {
        return Bindings.createBooleanBinding(
                () -> org.apache.commons.lang3.StringUtils.isBlank(textProperty.get()),
                textProperty
        );
    }

    public static BooleanBinding or(BooleanBinding b1, BooleanBinding b2, BooleanBinding b3) {
        return b1.or(b2.or(b3));
    }

    public static BooleanBinding or(BooleanProperty b1, BooleanProperty b2, BooleanProperty b3, BooleanProperty b4) {
        return b1.or(b2.or(b3.or(b4)));
    }
}
