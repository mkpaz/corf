package corf.base.desktop;

import javafx.beans.WeakListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public final class Observables {

    public static void rebind(BooleanProperty property, ObservableValue<? extends Boolean> obs) {
        property.unbind();
        property.bind(obs);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Boolean bindings                                                      //
    ///////////////////////////////////////////////////////////////////////////

    @SafeVarargs
    public static BooleanBinding and(ObservableValue<Boolean>... obs) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(obs).allMatch(ObservableValue::getValue), obs
        );
    }

    @SuppressWarnings("unchecked")
    public static BooleanBinding and(Collection<ObservableValue<Boolean>> obs) {
        return and(obs.toArray(new ObservableValue[0]));
    }

    @SafeVarargs
    public static BooleanBinding or(ObservableValue<Boolean>... obs) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(obs).anyMatch(ObservableValue::getValue), obs
        );
    }

    @SuppressWarnings("unchecked")
    public static BooleanBinding or(Collection<ObservableValue<Boolean>> obs) {
        return or(obs.toArray(new ObservableValue[0]));
    }

    public static <E> BooleanBinding ofPredicate(ObservableObjectValue<E> obs,
                                                 Predicate<E> predicate) {
        return ofPredicate(obs, predicate, null);
    }

    /**
     * Creates boolean binding that verifies that specified observable property
     * matches specified predicate. Optional transformer can be used to transform
     * contained value before checking. Transformer must not return null value.
     */
    public static <E> BooleanBinding ofPredicate(ObservableObjectValue<E> property,
                                                 Predicate<E> predicate,
                                                 @Nullable Function<E, E> transformer) {
        return Bindings.createBooleanBinding(() -> {

            if (isNull(property)) { return false; }
            E val = transformer == null ? property.get() : transformer.apply(property.get());
            return predicate.test(Objects.requireNonNull(val));
        }, property);
    }

    private static boolean isNull(ObservableObjectValue<?> obs) {
        return obs == null || obs.get() == null;
    }

    private static boolean isNotNull(ObservableObjectValue<?> obs) {
        return obs != null && obs.get() != null;
    }

    public static BooleanBinding isBlank(ObservableObjectValue<String> obs) {
        return Bindings.createBooleanBinding(() -> isNull(obs) || obs.get().isBlank(), obs);
    }

    public static BooleanBinding isNotBlank(ObservableObjectValue<String> obs) {
        return Bindings.createBooleanBinding(() -> isNotNull(obs) && !obs.get().isBlank(), obs);
    }

    public static BooleanBinding startsWith(ObservableObjectValue<String> obs, String prefix) {
        return startsWith(obs, prefix, null);
    }

    public static BooleanBinding startsWith(ObservableObjectValue<String> obs,
                                            String prefix,
                                            @Nullable Function<String, String> converter) {
        return ofPredicate(obs, val -> prefix != null && val.startsWith(prefix), converter);
    }

    public static BooleanBinding endsWith(ObservableObjectValue<String> obs, String suffix) {
        return endsWith(obs, suffix, null);
    }

    public static BooleanBinding endsWith(ObservableObjectValue<String> obs,
                                          String suffix,
                                          @Nullable Function<String, String> converter) {
        return ofPredicate(obs, val -> suffix != null && val.endsWith(suffix), converter);
    }

    public static <E> BooleanBinding contains(ObservableObjectValue<E> obs, Collection<E> c) {
        return contains(obs, c, null);
    }

    public static <E> BooleanBinding contains(ObservableObjectValue<E> obs,
                                              Collection<E> c,
                                              @Nullable Function<E, E> converter) {
        return ofPredicate(obs, val -> c != null && c.contains(val), converter);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Property bindings                                                       //
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public static <T> void bindToggleGroup(ToggleGroup toggleGroup, ObjectProperty<T> property) {
        for (Toggle toggle : toggleGroup.getToggles()) {
            if (toggle.getUserData() == null) {
                throw new IllegalArgumentException("The ToggleGroup contains at least one Toggle without user data.");
            }

            if (Objects.equals(property.get(), toggle.getUserData())) {
                toggleGroup.selectToggle(toggle);
                break;
            }
        }

        toggleGroup.selectedToggleProperty().addListener((obs, old, val) -> property.set((T) val.getUserData()));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Object bindings                                                       //
    ///////////////////////////////////////////////////////////////////////////

    public static <S, R> ObjectBinding<R> map(ObservableValue<S> source,
                                              Function<? super S, ? extends R> function,
                                              R defaultValue) {
        return Bindings.createObjectBinding(() -> {
            S sourceValue = source.getValue();

            if (sourceValue == null) {
                return defaultValue;
            } else {
                return function.apply(sourceValue);
            }
        }, source);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Content mapping                                                       //
    ///////////////////////////////////////////////////////////////////////////

    public static <E, F> void mapContent(ObservableList<F> mapped,
                                         ObservableList<? extends E> source,
                                         Function<? super E, ? extends F> mapper) {
        map(mapped, source, mapper);
    }

    private static <E, F> void map(ObservableList<F> mapped,
                                   ObservableList<? extends E> source,
                                   Function<? super E, ? extends F> mapper) {
        final ListContentMapping<E, F> contentMapping = new ListContentMapping<>(mapped, mapper);
        mapped.setAll(source.stream().map(mapper).collect(toList()));
        source.removeListener(contentMapping);
        source.addListener(contentMapping);
    }

    private static class ListContentMapping<E, F> implements ListChangeListener<E>, WeakListener {

        private final WeakReference<List<F>> mappedRef;
        private final Function<? super E, ? extends F> mapper;

        public ListContentMapping(List<F> mapped, Function<? super E, ? extends F> mapper) {
            this.mappedRef = new WeakReference<>(mapped);
            this.mapper = mapper;
        }

        @Override
        public void onChanged(Change<? extends E> change) {
            final List<F> mapped = mappedRef.get();
            if (mapped == null) {
                change.getList().removeListener(this);
            } else {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        mapped.subList(change.getFrom(), change.getTo()).clear();
                        mapped.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo())
                                .stream().map(mapper).toList());
                    } else {
                        if (change.wasRemoved()) {
                            mapped.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                        }
                        if (change.wasAdded()) {
                            mapped.addAll(change.getFrom(), change.getAddedSubList()
                                    .stream().map(mapper).toList());
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return mappedRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List<F> list = mappedRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) {
                return true;
            }

            final List<F> mapped1 = mappedRef.get();
            if (mapped1 == null) {
                return false;
            }

            if (that instanceof ListContentMapping<?, ?> other) {
                final List<?> mapped2 = other.mappedRef.get();
                return mapped1 == mapped2;
            }
            return false;
        }
    }
}
