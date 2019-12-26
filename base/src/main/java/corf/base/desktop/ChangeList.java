package corf.base.desktop;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Convenient wrapper that unites original {@link ObservableList} and its
 * transformation derivatives under the single roof.
 */
public class ChangeList<E> {

    protected final ObjectProperty<ObservableList<E>> items = new SimpleObjectProperty<>();
    protected final ObjectBinding<FilteredList<E>> filteredList;
    protected final ObjectBinding<SortedList<E>> sortedList;

    public ChangeList() {
        this(FXCollections.observableArrayList());
    }

    public ChangeList(@Nullable ObservableList<E> list) {
        // order matters, non-null items value should be set
        // before the first bindings evaluation
        setItems(list);

        filteredList = Bindings.createObjectBinding(
                () -> new FilteredList<>(itemsProperty().get()), itemsProperty()
        );

        sortedList = Bindings.createObjectBinding(
                () -> new SortedList<>(filteredList.get()), filteredList
        );
    }

    public ObservableList<E> getItems() {
        return items.get();
    }

    public ObjectProperty<ObservableList<E>> itemsProperty() {
        return items;
    }

    public void setItems(@Nullable ObservableList<E> list) {
        if (list != null && list.getClass().isAssignableFrom(TransformationList.class)) {
            throw new IllegalArgumentException("TransformationList cannot be used as data source.");
        }
        items.set(Objects.requireNonNullElse(list, FXCollections.observableArrayList()));
    }

    public FilteredList<E> getFilteredList() {
        return filteredList.get();
    }

    public ObjectBinding<FilteredList<E>> filteredListProperty() {
        return filteredList;
    }

    public SortedList<E> getSortedList() {
        return sortedList.get();
    }

    public ObjectBinding<SortedList<E>> sortedListProperty() {
        return sortedList;
    }

    @Override
    public String toString() {
        return String.valueOf(items.get());
    }
}
