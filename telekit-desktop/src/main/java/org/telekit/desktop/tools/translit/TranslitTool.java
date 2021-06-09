package org.telekit.desktop.tools.translit;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class TranslitTool implements Tool<TranslitController> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_TRANSLIT); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<TranslitController> getComponent() { return TranslitController.class; }

    @Override
    public Node getIcon() { return null; }
}