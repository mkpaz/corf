package telekit.desktop.tools.passgen;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.desktop.i18n.DesktopMessages;

import static telekit.base.i18n.I18n.t;

public final class PasswordGeneratorTool implements Tool<PasswordGeneratorView> {

    @Override
    public String getName() {
        return t(DesktopMessages.TOOL_PASSWORD_GENERATOR);
    }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<PasswordGeneratorView> getComponent() { return PasswordGeneratorView.class; }

    @Override
    public Node getIcon() { return null; }
}