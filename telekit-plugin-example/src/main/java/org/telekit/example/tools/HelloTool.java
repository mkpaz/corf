package org.telekit.example.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.UILoader;
import org.telekit.base.fx.Controller;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Tool;
import org.telekit.example.ExamplePlugin;
import org.telekit.example.MessageKeys;

import java.net.URL;

import static org.telekit.example.ExamplePlugin.ASSETS_PATH;

public class HelloTool implements Tool {

    @Override
    public @NotNull String getName() {
        return ExamplePlugin.class.getSimpleName() + ": " + Messages.get(MessageKeys.HELLO);
    }

    @Override
    public @Nullable String getGroupName() {
        return null;
    }

    @Override
    public @NotNull Controller createController() {
        URL fxmlLocation = getClass().getResource(ASSETS_PATH + "ui/example.fxml");
        return UILoader.load(fxmlLocation, ExamplePlugin.class);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
