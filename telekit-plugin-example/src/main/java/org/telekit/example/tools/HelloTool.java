package org.telekit.example.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.i18n.I18n;
import org.telekit.base.plugin.Tool;
import org.telekit.example.ExamplePlugin;
import org.telekit.example.i18n.ExampleMessages;

public class HelloTool implements Tool {

    @Override
    public @NotNull String getName() {
        return ExamplePlugin.class.getSimpleName() + ": " + I18n.t(ExampleMessages.HELLO);
    }

    @Override
    public @Nullable String getGroupName() {
        return null;
    }

    @Override
    public @NotNull Component createComponent() {
        return ViewLoader.load(ExampleController.class);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
