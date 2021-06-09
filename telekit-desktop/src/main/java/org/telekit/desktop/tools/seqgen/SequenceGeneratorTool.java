package org.telekit.desktop.tools.seqgen;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class SequenceGeneratorTool implements Tool<SequenceGeneratorController> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_SEQ_GEN); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<SequenceGeneratorController> getComponent() { return SequenceGeneratorController.class; }

    @Override
    public Node getIcon() { return null; }
}