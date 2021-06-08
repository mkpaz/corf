package org.telekit.base.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.desktop.mvvm.ViewModel;
import org.telekit.base.i18n.I18n;

public interface Tool extends Extension {

    /**
     * Tool name represents corresponding menu item.
     * Use {@link Messages#get} if you want internationalize tool name.
     */
    @NotNull String getName();

    /**
     * Group name represents menu group for several tools.
     * Use {@link Messages#get} if you want internationalize group name.
     */
    @Nullable String getGroupName();

    /**
     * JavaFX controller for the tool stage. It will be created only when user
     * will actually choose to run this tool.
     */
    @NotNull Component createComponent();

    /**
     * Specifies will tool be opened in modal window or not.
     */
    boolean isModal();
}
