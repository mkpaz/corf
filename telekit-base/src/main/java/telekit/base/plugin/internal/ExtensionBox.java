package telekit.base.plugin.internal;

import telekit.base.plugin.Extension;
import telekit.base.plugin.Plugin;

public class ExtensionBox {

    private final Extension extension;
    private final Class<? extends Plugin> pluginClass;

    public ExtensionBox(Extension extension, Class<? extends Plugin> pluginClass) {
        this.extension = extension;
        this.pluginClass = pluginClass;
    }

    public Extension getExtension() {
        return extension;
    }

    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }

    public boolean isProvidedByPlugin(Class<? extends Plugin> pluginClass) {
        return this.pluginClass.equals(pluginClass);
    }

    @Override
    public String toString() {
        return "ExtensionBox{" +
                "extension=" + extension +
                ", pluginClass=" + pluginClass +
                '}';
    }
}
