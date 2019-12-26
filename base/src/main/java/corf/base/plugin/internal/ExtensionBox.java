package corf.base.plugin.internal;

import corf.base.plugin.Extension;
import corf.base.plugin.Plugin;

import java.util.Objects;

/** The container for a plugin extension. */
public final class ExtensionBox {

    private final Extension extension;
    private final Class<? extends Plugin> pluginClass;

    public ExtensionBox(Extension extension,
                        Class<? extends Plugin> pluginClass) {
        this.extension = Objects.requireNonNull(extension, "extension");
        this.pluginClass = Objects.requireNonNull(pluginClass, "pluginClass");
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
