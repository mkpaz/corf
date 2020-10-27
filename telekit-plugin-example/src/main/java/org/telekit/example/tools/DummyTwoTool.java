package org.telekit.example.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.ui.Controller;
import org.telekit.base.plugin.Tool;

@SuppressWarnings("ConstantConditions")
public class DummyTwoTool implements Tool {

    @Override
    public @NotNull String getName() {
        return "Dummy Two";
    }

    @Override
    public @Nullable String getGroupName() {
        return "Dummy";
    }

    @Override
    public @NotNull Controller createController() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
