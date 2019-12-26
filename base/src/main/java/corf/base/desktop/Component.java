package corf.base.desktop;

import backbonefx.mvvm.View;
import backbonefx.mvvm.ViewModel;
import javafx.scene.Node;

/**
 * Represents a {@link View} with no {@link ViewModel} in cases where
 * the latter isn't necessary.
 */
public interface Component<V extends Node> extends View<V, Component.EmptyViewModel> {

    EmptyViewModel INSTANCE = new EmptyViewModel();

    @Override
    default EmptyViewModel getViewModel() {
        return INSTANCE;
    }

    final class EmptyViewModel implements ViewModel { }
}
