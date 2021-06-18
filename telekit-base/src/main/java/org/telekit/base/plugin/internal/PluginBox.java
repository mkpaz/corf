package org.telekit.base.plugin.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.plugin.Extension;
import org.telekit.base.plugin.Plugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.telekit.base.Env.getPluginConfigDir;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.base.util.FileUtils.isDirEmpty;

public class PluginBox {

    private final Plugin plugin;
    private PluginState state;
    private final Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionTypes;

    public PluginBox(@NotNull Plugin plugin, @NotNull PluginState state) {
        this.plugin = plugin;
        this.state = state;
        this.extensionTypes = PluginLoader.resolveExtensionTypes(getPluginClass());
    }

    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    public @NotNull Class<? extends Plugin> getPluginClass() {
        return plugin.getClass();
    }

    public PluginState getState() {
        return state;
    }

    public void setState(@NotNull PluginState state) {
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
    public @NotNull Collection<Class<? extends Extension>> getExtensionsOfType(
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
        return Files.exists(configDir) && !isDirEmpty(configDir);
    }

    public @NotNull Collection<Path> getConfigs() {
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
                "plugin=" + className(getPluginClass()) +
                ", state=" + state +
                '}';
    }
}
