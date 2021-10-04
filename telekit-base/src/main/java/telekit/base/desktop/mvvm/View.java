package telekit.base.desktop.mvvm;

import telekit.base.desktop.Component;

public interface View<M extends ViewModel> extends Component {

    M getViewModel();
}
