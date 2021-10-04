package telekit.base.desktop.mvvm;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public abstract class CommandBase implements Command {

    protected final ReadOnlyBooleanWrapper executable = new ReadOnlyBooleanWrapper(true);

    public final ReadOnlyBooleanProperty executableProperty() { return executable.getReadOnlyProperty(); }

    @Override
    public void execute() {
        if (isExecutable()) { doExecute(); }
    }

    protected void doExecute() {}

    @Override
    public final boolean isExecutable() {
        return executableProperty().get();
    }
}