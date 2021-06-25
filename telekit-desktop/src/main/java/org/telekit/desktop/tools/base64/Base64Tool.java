package org.telekit.desktop.tools.base64;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class Base64Tool implements Tool<Base64View> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_BASE64); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<Base64View> getComponent() { return Base64View.class; }

    @Override
    public Node getIcon() { return null; }
}