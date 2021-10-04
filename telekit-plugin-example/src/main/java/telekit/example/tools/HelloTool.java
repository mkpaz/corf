package telekit.example.tools;

import javafx.scene.Node;
import telekit.base.plugin.Tool;
import telekit.base.plugin.ToolGroup;
import telekit.example.ExamplePlugin;

public class HelloTool implements Tool<ExampleController> {

    @Override
    public String getName() { return ExamplePlugin.class.getSimpleName(); }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<ExampleController> getComponent() { return ExampleController.class; }

    @Override
    public Node getIcon() { return null; }
}
