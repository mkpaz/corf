package telekit.desktop.tools.ipcalc;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.desktop.i18n.DesktopMessages;

import static telekit.base.i18n.I18n.t;

public final class IPv4CalcTool implements Tool<IPv4CalcView> {

    @Override
    public String getName() { return t(DesktopMessages.TOOL_IPV4_CALCULATOR); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<IPv4CalcView> getComponent() { return IPv4CalcView.class; }

    @Override
    public Node getIcon() { return null; }
}