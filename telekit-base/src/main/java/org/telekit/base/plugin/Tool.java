package org.telekit.base.plugin;

import javafx.scene.Node;
import org.telekit.base.desktop.Component;

public interface Tool<C extends Component> extends Extension {

    /* Use {@link I18n#translate(String)} if you want to internationalize tool name */
    String getName();

    /* Use {@link I18n#translate(String)} if you want to internationalize tool name */
    ToolGroup getGroup();

    Class<C> getComponent();

    Node getIcon();

    default String id() {
        return getClass().getCanonicalName();
    }
}
