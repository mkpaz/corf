package org.telekit.controls.mvvm;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.layout.Region;

import static org.telekit.base.i18n.I18n.t;

public interface View<M extends ViewModel> {

    Region getRoot();

    M getViewModel();

    default StringBinding createI18nBinding(ObservableStringValue prop) {
        return Bindings.createStringBinding(() -> prop != null && prop.get() != null ? t(prop.get()) : null, prop);
    }
}
