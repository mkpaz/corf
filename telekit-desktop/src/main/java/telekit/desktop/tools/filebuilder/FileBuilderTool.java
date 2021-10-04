package telekit.desktop.tools.filebuilder;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.desktop.i18n.DesktopMessages;

import static telekit.base.i18n.I18n.t;

public final class FileBuilderTool implements Tool<FileBuilderView> {

    @Override
    public String getName() { return t(DesktopMessages.TOOL_IMPORT_FILE_BUILDER); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<FileBuilderView> getComponent() { return FileBuilderView.class; }

    @Override
    public Node getIcon() { return null; }
}