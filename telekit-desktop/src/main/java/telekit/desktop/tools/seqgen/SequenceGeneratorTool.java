package telekit.desktop.tools.seqgen;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.desktop.i18n.DesktopMessages;

import static telekit.base.i18n.I18n.t;

public final class SequenceGeneratorTool implements Tool<SequenceGeneratorView> {

    @Override
    public String getName() { return t(DesktopMessages.TOOL_SEQUENCE_GENERATOR); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<SequenceGeneratorView> getComponent() { return SequenceGeneratorView.class; }

    @Override
    public Node getIcon() { return null; }
}