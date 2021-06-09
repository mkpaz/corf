package org.telekit.desktop.tools.ss7;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class SPCConverterTool implements Tool<SPCConverterController> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_SS7_SPC_CONV); }

    @Override
    public ToolGroup getGroup() { return SS7ToolGroup.VALUE; }

    @Override
    public Class<SPCConverterController> getComponent() { return SPCConverterController.class; }

    @Override
    public Node getIcon() { return null; }
}