package org.telekit.example.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.Component;
import org.telekit.base.plugin.Tool;

@SuppressWarnings("ConstantConditions")
public class DummyOneTool implements Tool {

    @Override
    public @NotNull String getName() {
        return "Dummy One";
    }

    @Override
    public @Nullable String getGroupName() {
        return "Dummy";
    }

    @Override
    public @NotNull Component createComponent() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
