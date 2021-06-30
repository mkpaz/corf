package org.telekit.desktop.tools.apiclient;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

public final class ApiClientTool implements Tool<ApiClientView> {

    @Override
    public String getName() { return t(DesktopMessages.TOOL_API_CLIENT); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<ApiClientView> getComponent() { return ApiClientView.class; }

    @Override
    public Node getIcon() { return null; }
}
