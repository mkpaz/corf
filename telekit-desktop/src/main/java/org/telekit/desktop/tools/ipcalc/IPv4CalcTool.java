package org.telekit.desktop.tools.ipcalc;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class IPv4CalcTool implements Tool<IPv4Controller> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_IP4_CALC); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<IPv4Controller> getComponent() { return IPv4Controller.class; }

    @Override
    public Node getIcon() { return null; }
}