package org.telekit.example.tools;

import org.telekit.base.UILoader;
import org.telekit.base.fx.Controller;
import org.telekit.base.plugin.Tool;
import org.telekit.example.ExamplePlugin;

import java.net.URL;

import static org.telekit.example.ExamplePlugin.ASSETS_PATH;

public class ExampleTool implements Tool {

    private static final String NAME = "Example";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Controller createController() {
        URL fxmlLocation = getClass().getResource(ASSETS_PATH + "ui/example.fxml");
        return UILoader.load(fxmlLocation, ExamplePlugin.class);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
