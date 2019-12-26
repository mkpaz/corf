package corf.base.plugin;

import backbonefx.mvvm.View;
import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;

/** The type of extension that represents a graphical utility. */
public interface Tool<V extends View<?, ?>> extends Extension {

    String getName();

    Class<V> getView();

    @Nullable ToolGroup getGroup();

    @Nullable Image getIcon();

    default String id() {
        return getClass().getCanonicalName();
    }
}
