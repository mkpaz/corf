package org.telekit.controls.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValueBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * According to {@link ObservableValueBase} docs, which is the base
 * for any observable property class, all listeners will be notified
 * "whenever the value of the ObservableValue changes".
 * <p>
 * By "changes" it means only equality, not identity. While it's correct
 * for primitives, but it's very unfortunate implementation for object
 * properties. For entities it usually means that change event won't be
 * fired unless object ID/PK changes, which is unacceptable.
 * <p>
 * This is an observable property implementation which will also notify
 * listeners when value identity changes.
 */
public class IdentityObjectProperty<T> extends SimpleObjectProperty<T> {

    private List<ChangeListener<? super T>> changeListeners;

    public IdentityObjectProperty(T initialValue) {
        super(initialValue);
    }

    public IdentityObjectProperty(Object bean, String name) {
        super(bean, name);
    }

    public IdentityObjectProperty(Object bean, String name, T initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void set(T newValue) {
        T oldValue = get();

        // base implementation only notifies listeners if values are not equal
        super.set(newValue);

        if (changeListeners != null && Objects.equals(oldValue, newValue) && oldValue != newValue) {
            changeListeners.forEach(l -> l.changed(this, oldValue, newValue));
        }
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        super.addListener(listener);

        changeListeners = changeListeners != null ? changeListeners : new ArrayList<>(1);
        changeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        super.removeListener(listener);

        if (changeListeners != null) { changeListeners.remove(listener); }
    }
}