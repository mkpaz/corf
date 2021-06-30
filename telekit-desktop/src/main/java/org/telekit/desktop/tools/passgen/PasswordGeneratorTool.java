package org.telekit.desktop.tools.passgen;

import javafx.scene.Node;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.desktop.i18n.DesktopMessages;

import static org.telekit.base.i18n.I18n.t;

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