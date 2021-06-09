package org.telekit.controls.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;

import java.util.Objects;

import static javafx.collections.FXCollections.observableArrayList;

public class TransformationListHandle<E> {

    private final ObjectProperty<ObservableList<E>> items = new SimpleObjectProperty<>(this, "items", observableArrayList());
    private final ObjectBinding<FilteredList<E>> filteredList;
    private final ObjectBinding<SortedList<E>> sortedList;

    public TransformationListHandle() {
        this(null);
    }

    public TransformationListHandle(ObservableList<E> items) {
        filteredList = Bindings.createObjectBinding(() -> new FilteredList<>(itemsProperty().get()), itemsProperty());
        sortedList = Bindings.createObjectBinding(() -> new SortedList<>(filteredList.get()), filteredList);

        setItems(items);
    }

    public ObservableList<E> getItems() { return items.get(); }

    public ObjectProperty<ObservableList<E>> itemsProperty() { return items; }

    public void setItems(ObservableList<E> items) {
        if (items != null && items.getClass().isAssignableFrom(TransformationList.class)) {
            throw new IllegalArgumentException("Immutable list cannot be used as data source");
        }
        this.items.set(Objects.requireNonNullElse(items, observableArrayList()));
    }

    public FilteredList<E> getFilteredList() { return filteredList.get(); }

    public ObjectBinding<FilteredList<E>> filteredListProperty() { return filteredList; }

    public SortedList<E> getSortedList() { return sortedList.get(); }

    public ObjectBinding<SortedList<E>> sortedListProperty() { return sortedList; }

    @Override
    public String toString() { return String.valueOf(items.get()); }
}
