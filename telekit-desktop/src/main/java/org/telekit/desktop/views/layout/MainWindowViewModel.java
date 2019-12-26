package org.telekit.desktop.views.layout;

import org.telekit.base.desktop.mvvm.ViewModel;
import org.telekit.base.desktop.routing.Router;
import org.telekit.base.di.Initializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.telekit.desktop.startup.config.Config.DEFAULT_ROUTE;

@Singleton
public class MainWindowViewModel implements Initializable, ViewModel {

    private final Router router;

    @Inject
    public MainWindowViewModel(Router router) {
        this.router = router;
    }

    @Override
    public void initialize() {
        router.navigate(DEFAULT_ROUTE);
    }
}
