package org.telekit.base.desktop.mvvm;

import org.telekit.base.desktop.Component;

public interface View<M extends ViewModel> extends Component {

    M getViewModel();
}
