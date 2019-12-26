package corf.base.plugin.internal;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.plugin.Extension;
import corf.base.plugin.Plugin;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** The container for a plugin. */
public final class PluginBox {

    private final Plugin plugin;
    private PluginState state;
    private final Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionTypes;

    public PluginBox(Plugin plugin, PluginState state) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.state = Objects.requireNonNull(state, "state");
        this.extensionTypes = PluginLoader.resolveExtensionTypes(getPluginClass());
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Class<? extends Plugin> getPluginClass() {
        return plugin.getClass();
    }

    public PluginState getState() {
        return state;
    }

    public void setState(PluginState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    /**
     * Checks if plugin provides specified extension implementation.
     *
     * @param implClass concrete class implementing one of extension interfaces
     */
    public boolean providesExtensionImpl(Class<? extends Extension> implClass) {
        Objects.requireNonNull(implClass, "implClass");
        return extensionTypes.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(cls -> cls.equals(implClass));
    }

    /**
     * Checks if plugin provides any extensions of specified type.
     *
     * @param type extension type (interface)
     */
    public boolean providesExtensionsOfType(Class<? extends Extension> type) {
        Objects.requireNonNull(type, "type");
        return extensionTypes.containsKey(type);
    }

    /**
     * Returns extensions of specified type provided by the plugin.
     *
     * @param type extension type (interface)
     */
    public Collection<Class<? extends Extension>> getExtensionsOfType(Class<? extends Extension> type) {
        Objects.requireNonNull(type, "type");
        return extensionTypes.containsKey(type) ?
                new ArrayList<>(extensionTypes.get(type)) :
                Collections.emptyList();
    }

    public @Nullable Path getJarPath() {
        URL jarLocation = plugin.getLocation();
        if (jarLocation == null) { return null; }

        try {
            return Paths.get(jarLocation.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public String getPluginClassName() {
        return ClassUtils.getCanonicalName(getPluginClass());
    }

    @Override
    public String toString() {
        return "PluginContainer{" +
                "plugin=" + getPluginClassName() +
                ", state=" + state +
                '}';
    }
}
