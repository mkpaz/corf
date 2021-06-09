package org.telekit.desktop.views.system;

import org.telekit.base.desktop.mvvm.ViewModel;
import org.telekit.base.di.Initializable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WelcomeViewModel implements Initializable, ViewModel {

    @Inject
    public WelcomeViewModel() {}

    @Override
    public void initialize() {}
}
