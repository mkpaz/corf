package org.telekit.desktop.views.layout;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.telekit.base.Env;
import org.telekit.base.desktop.mvvm.ViewModel;
import org.telekit.base.desktop.routing.Router;
import org.telekit.base.di.Initializable;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.desktop.event.PendingRestartEvent;
import org.telekit.desktop.views.NavLink;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TitleBarViewModel implements Initializable, ViewModel {

    private final Router router;

    @Inject
    public TitleBarViewModel(Router router) {
        this.router = router;
    }

    @Override
    public void initialize() {
        router.currentRouteProperty().addListener((observable, old, value) -> {
            if (value == null) { return; }
            String arg = value.getArg(NavLink.Arg.APP_TITLE, String.class);
            appTitle.set(arg != null ? arg : Env.APP_NAME);
        });

        DefaultEventBus.getInstance().subscribe(PendingRestartEvent.class, e -> restartPending.set(true));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    private final ReadOnlyStringWrapper appTitle = new ReadOnlyStringWrapper(this, "appTitle", Env.APP_NAME);

    public ReadOnlyStringProperty appTitleProperty() { return appTitle.getReadOnlyProperty(); }

    private final ReadOnlyBooleanWrapper restartPending = new ReadOnlyBooleanWrapper(this, "restartPending", false);

    public ReadOnlyBooleanProperty restartPendingProperty() { return restartPending.getReadOnlyProperty(); }
}
