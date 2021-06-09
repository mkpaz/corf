package org.telekit.desktop.tools.ss7;

import javafx.scene.Node;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class SS7ToolGroup implements ToolGroup {

    public static final ToolGroup VALUE = new SS7ToolGroup();

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_GROUP_SS7); }

    @Override
    public boolean isExpanded() { return true; }

    @Override
    public Node getIcon() { return null; }
}
