package telekit.desktop.tools.apiclient;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.desktop.i18n.DesktopMessages;

import static telekit.base.i18n.I18n.t;

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
