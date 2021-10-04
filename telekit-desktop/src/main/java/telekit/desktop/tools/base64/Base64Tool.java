package telekit.desktop.tools.base64;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.desktop.i18n.DesktopMessages;

import static telekit.base.i18n.I18n.t;

public final class Base64Tool implements Tool<Base64View> {

    @Override
    public String getName() { return t(DesktopMessages.TOOL_BASE64_ENCODER); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<Base64View> getComponent() { return Base64View.class; }

    @Override
    public Node getIcon() { return null; }
}