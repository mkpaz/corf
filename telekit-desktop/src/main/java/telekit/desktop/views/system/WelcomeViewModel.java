package telekit.desktop.views.system;

import telekit.base.desktop.mvvm.ViewModel;
import telekit.base.di.Initializable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WelcomeViewModel implements Initializable, ViewModel {

    @Inject
    public WelcomeViewModel() {}

    @Override
    public void initialize() {}
}
