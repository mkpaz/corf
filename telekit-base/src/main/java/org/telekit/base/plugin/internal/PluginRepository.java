package org.telekit.base.plugin.internal;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.plugin.Extension;
import org.telekit.base.plugin.Plugin;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PluginRepository {

    private final Map<Class<? extends Plugin>, PluginBox> plugins = new HashMap<>();

    public Optional<PluginBox> get(Class<? extends Plugin> pluginClass) {
        return Optional.ofNullable(plugins.get(pluginClass));
    }

    public List<PluginBox> findAll() {
        return new ArrayList<>(plugins.values());
    }

    public List<PluginBox> find(Predicate<PluginBox> condition) {
        return plugins.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    public boolean contains(Class<? extends Plugin> pluginClass) {
        return plugins.containsKey(pluginClass);
    }

    public void put(PluginBox pluginBox) {
        plugins.putIfAbsent(pluginBox.getPluginClass(), pluginBox);
    }

    public void remove(Class<? extends Plugin> pluginClass) {
        plugins.remove(pluginClass);
    }

    public @NotNull Collection<PluginBox> findPluginsThatProvide(Class<? extends Extension> extensionType) {
        return plugins.values().stream()
                .filter(container -> container.providesExtensionsOfType(extensionType))
                .collect(Collectors.toList());
    }

    public Optional<PluginBox> whatPluginProvides(Class<? extends Extension> implClass) {
        return plugins.values().stream()
                .filter(pluginBox -> pluginBox.providesExtensionImpl(implClass))
                .findFirst();
    }
}
