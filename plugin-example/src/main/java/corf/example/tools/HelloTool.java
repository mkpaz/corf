package corf.example.tools;

import javafx.scene.image.Image;
import corf.base.plugin.Tool;
import corf.base.plugin.ToolGroup;
import corf.example.ExamplePlugin;

public final class HelloTool implements Tool<ExampleView> {

    private static final ToolGroup GROUP = () -> "Example Plugin";

    @Override
    public String getName() {
        return "Hello Tool";
    }

    @Override
    public ToolGroup getGroup() {
        return GROUP;
    }

    @Override
    public Class<ExampleView> getView() {
        return ExampleView.class;
    }

    @Override
    public Image getIcon() {
        return ExamplePlugin.PLUGIN_ICON;
    }
}
