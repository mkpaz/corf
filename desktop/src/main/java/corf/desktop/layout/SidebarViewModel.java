package corf.desktop.layout;

import backbonefx.mvvm.Command;
import backbonefx.mvvm.RunnableCommand;
import backbonefx.mvvm.ViewModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

@Singleton
public final class SidebarViewModel implements ViewModel {

    @Inject
    public SidebarViewModel() { }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    private final ReadOnlyBooleanWrapper navDrawerOpened = new ReadOnlyBooleanWrapper();

    public ReadOnlyBooleanProperty navDrawerOpenedProperty() { return navDrawerOpened.getReadOnlyProperty(); }

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == toggleNavDrawerCommand ==

    private final Command<Void> toggleNavDrawerCommand = new RunnableCommand(
            () -> navDrawerOpened.set(!navDrawerOpened.get())
    );

    public Runnable toggleNavDrawerCommand() {
        return (RunnableCommand) toggleNavDrawerCommand;
    }

    // == hideNavDrawerCommand ==

    private final Command<Void> hideNavDrawerCommand = new RunnableCommand(
            () -> navDrawerOpened.set(false)
    );

    public Runnable hideNavDrawerCommand() {
        return (RunnableCommand) hideNavDrawerCommand;
    }
}
