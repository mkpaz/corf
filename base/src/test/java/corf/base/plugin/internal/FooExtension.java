package corf.base.plugin.internal;

import javafx.scene.image.Image;
import corf.base.desktop.Component;
import corf.base.plugin.Tool;
import corf.base.plugin.ToolGroup;

@SuppressWarnings("ALL")
public class FooExtension implements Tool {

    @Override
    public String getName() { return null; }

    @Override
    public ToolGroup getGroup() { return null; }

    @Override
    public Class<? extends Component> getView() { return null; }

    @Override
    public Image getIcon() { return null; }
}
