package telekit.desktop.views.layout;

import telekit.base.desktop.mvvm.ViewModel;
import telekit.base.desktop.routing.Router;
import telekit.base.di.Initializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import static telekit.desktop.startup.config.Config.DEFAULT_ROUTE;

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
