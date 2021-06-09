package org.telekit.desktop.tools.ss7;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class CICTableTool implements Tool<CICTableController> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_SS7_CIC_TABLE); }

    @Override
    public ToolGroup getGroup() { return SS7ToolGroup.VALUE; }

    @Override
    public Class<CICTableController> getComponent() { return CICTableController.class; }

    @Override
    public Node getIcon() { return null; }
}