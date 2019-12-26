package org.telekit.base.desktop.mvvm;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public abstract class ConsumerCommandBase<T> implements ConsumerCommand<T> {

    protected final ReadOnlyBooleanWrapper executable = new ReadOnlyBooleanWrapper(true);

    public final ReadOnlyBooleanProperty executableProperty() { return executable.getReadOnlyProperty(); }

    @Override
    public void execute(T t) {
        if (isExecutable()) { doExecute(t); }
    }

    protected void doExecute(T t) {}

    @Override
    public final boolean isExecutable() {
        return executableProperty().get();
    }
}