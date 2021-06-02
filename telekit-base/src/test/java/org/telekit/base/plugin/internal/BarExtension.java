package org.telekit.base.plugin.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.plugin.Tool;

@SuppressWarnings("ConstantConditions")
public class BarExtension implements Tool {
    
    @Override
    public @NotNull String getName() {
        return null;
    }

    @Override
    public @Nullable String getGroupName() {
        return null;
    }

    @Override
    public @NotNull View<?> createComponent() {
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}