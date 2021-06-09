package org.telekit.desktop.tools.filebuilder;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class FileBuilderTool implements Tool<FileBuilderController> {

    @Override
    public String getName() { return t(DesktopMessages.TOOLS_FILE_BUILDER); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<FileBuilderController> getComponent() { return FileBuilderController.class; }

    @Override
    public Node getIcon() { return null; }
}