package org.telekit.controls.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.Collection;

public final class ExtraBindings {

    @SafeVarargs
    public static BooleanBinding and(ObservableValue<Boolean>... values) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(values).allMatch(ObservableValue::getValue), values
        );
    }

    @SuppressWarnings("unchecked")
    public static BooleanBinding and(Collection<ObservableValue<Boolean>> values) {
        return and(values.toArray(new ObservableValue[0]));
    }

    @SafeVarargs
    public static BooleanBinding or(ObservableValue<Boolean>... values) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(values).anyMatch(ObservableValue::getValue), values
        );
    }

    @SuppressWarnings("unchecked")
    public static BooleanBinding or(Collection<ObservableValue<Boolean>> values) {
        return or(values.toArray(new ObservableValue[0]));
    }

    public static BooleanBinding isBlank(StringProperty textProperty) {
        return Bindings.createBooleanBinding(() -> isBlank(textProperty.get()), textProperty);
    }

    private static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }
}
