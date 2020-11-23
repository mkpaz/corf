package org.telekit.controls.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ExtraBindings {

    @SafeVarargs
    public static BooleanBinding and(ObservableValue<Boolean>... properties) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(properties).allMatch(ObservableValue::getValue), properties
        );
    }

    @SuppressWarnings("unchecked")
    public static BooleanBinding and(Collection<ObservableValue<Boolean>> properties) {
        return and(properties.toArray(new ObservableValue[0]));
    }

    @SafeVarargs
    public static BooleanBinding or(ObservableValue<Boolean>... values) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(values).anyMatch(ObservableValue::getValue), values
        );
    }

    @SuppressWarnings("unchecked")
    public static BooleanBinding or(Collection<ObservableValue<Boolean>> properties) {
        return or(properties.toArray(new ObservableValue[0]));
    }

    public static BooleanBinding isBlank(ObservableObjectValue<String> property) {
        return Bindings.not(isNotBlank(property));
    }

    public static BooleanBinding isNotBlank(ObservableObjectValue<String> property) {
        return Bindings.createBooleanBinding(() -> isNotNullProperty(property) && !property.get().isBlank(), property);
    }

    public static BooleanBinding startsWith(ObservableObjectValue<String> value, String prefix) {
        return startsWith(value, prefix, null);
    }

    public static BooleanBinding startsWith(ObservableObjectValue<String> property,
                                            String prefix,
                                            Function<String, String> converter) {
        return ofPredicate(property, val -> prefix != null && val.startsWith(prefix), converter);
    }

    public static BooleanBinding endsWith(ObservableObjectValue<String> property, String suffix) {
        return endsWith(property, suffix, null);
    }

    public static BooleanBinding endsWith(ObservableObjectValue<String> property,
                                          String suffix,
                                          Function<String, String> converter) {
        return ofPredicate(property, val -> suffix != null && val.endsWith(suffix), converter);
    }

    public static <E> BooleanBinding contains(ObservableObjectValue<E> property, Collection<E> c) {
        return contains(property, c, null);
    }

    public static <E> BooleanBinding contains(ObservableObjectValue<E> property,
                                              Collection<E> c,
                                              Function<E, E> converter) {
        return ofPredicate(property, val -> c != null && c.contains(val), converter);
    }

    public static <E> BooleanBinding ofPredicate(ObservableObjectValue<E> property,
                                                 Predicate<E> predicate) {
        return ofPredicate(property, predicate, null);
    }

    /**
     * Creates boolean binding that verifies that specified observable property
     * matches specified predicate. Optional converter can be used to transform
     * contained value before checking (e.g. trim value). Converter must not
     * return null value.
     */
    public static <E> BooleanBinding ofPredicate(ObservableObjectValue<E> property,
                                                 Predicate<E> predicate,
                                                 Function<E, E> converter) {
        return Bindings.createBooleanBinding(() -> {
            if (isNullProperty(property)) return false;
            E val = converter == null ? property.get() : converter.apply(property.get());
            return predicate.test(Objects.requireNonNull(val));
        }, property);
    }

    private static boolean isNullProperty(ObservableObjectValue<?> property) {
        return !isNotNullProperty(property);
    }

    private static boolean isNotNullProperty(ObservableObjectValue<?> property) {
        return property != null && property.get() != null;
    }
}
