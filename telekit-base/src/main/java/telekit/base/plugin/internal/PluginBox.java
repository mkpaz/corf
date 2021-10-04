package telekit.base.plugin.internal;

import org.jetbrains.annotations.Nullable;
import telekit.base.plugin.Extension;
import telekit.base.plugin.Plugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ClassUtils.getCanonicalName;
import static telekit.base.Env.getPluginConfigDir;
import static telekit.base.util.FileSystemUtils.isEmptyDir;

public class PluginBox {

    private final Plugin plugin;
    private PluginState state;
    private final Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionTypes;

    public PluginBox(Plugin plugin, PluginState state) {
        this.plugin = plugin;
        this.state = state;
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
        this.state = state;
    }

    /**
     * Checks if plugin provides specified extension implementation.
     *
     * @param implClass concrete class implementing one of extension interfaces
     */
    public boolean providesExtensionImpl(Class<? extends Extension> implClass) {
        return extensionTypes.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(cls -> cls.equals(implClass));
    }

    /**
     * Checks if plugin provides any extensions of specified type.
     *
     * @param extensionType extension type (interface)
     */
    public boolean providesExtensionsOfType(Class<? extends Extension> extensionType) {
        return extensionTypes.containsKey(extensionType);
    }

    /**
     * Returns extensions of specified type provided by that plugin.
     *
     * @param extensionType extension type (interface)
     */
    public Collection<Class<? extends Extension>> getExtensionsOfType(
            Class<? extends Extension> extensionType) {
        return extensionTypes.containsKey(extensionType) ?
                new ArrayList<>(extensionTypes.get(extensionType)) :
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

    public boolean hasConfigs() {
        Path configDir = getPluginConfigDir(plugin.getClass());
        return Files.exists(configDir) && !isEmptyDir(configDir);
    }

    public Collection<Path> getConfigs() {
        Path configDir = getPluginConfigDir(plugin.getClass());
        if (!Files.exists(configDir)) { return Collections.emptyList(); }

        try {
            return Files.walk(configDir)
                    .filter(path -> !path.equals(configDir))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        return "PluginContainer{" +
                "plugin=" + getCanonicalName(getPluginClass()) +
                ", state=" + state +
                '}';
    }
}