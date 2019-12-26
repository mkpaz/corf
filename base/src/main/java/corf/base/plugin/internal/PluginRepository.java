package corf.base.plugin.internal;

import corf.base.plugin.Extension;
import corf.base.plugin.Plugin;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Plugin repository stores all the plugins that app knows about.
 * It uses plugin {@link Class} as unique plugin ID.
 */
final class PluginRepository {

    private final Map<Class<? extends Plugin>, PluginBox> plugins = new HashMap<>();

    /** Returns plugin container by given plugin class, if any. */
    public Optional<PluginBox> get(Class<? extends Plugin> pluginClass) {
        Objects.requireNonNull(pluginClass, "pluginClass");
        return Optional.ofNullable(plugins.get(pluginClass));
    }

    /** Returns the list of all known plugins. */
    public List<PluginBox> findAll() {
        return new ArrayList<>(plugins.values());
    }

    /** Searches for the plugin container by given predicate. */
    public List<PluginBox> find(Predicate<PluginBox> condition) {
        Objects.requireNonNull(condition, "condition");
        return plugins.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    /** Checks whether repository contains plugin container that matches given plugin class or not. */
    public boolean contains(Class<? extends Plugin> pluginClass) {
        Objects.requireNonNull(pluginClass, "pluginClass");
        return plugins.containsKey(pluginClass);
    }

    /** Registers plugin in the repository. */
    public void put(PluginBox pluginBox) {
        Objects.requireNonNull(pluginBox, "pluginBox");
        plugins.putIfAbsent(pluginBox.getPluginClass(), pluginBox);
    }

    /** Removes plugin from the repository. */
    public void remove(Class<? extends Plugin> pluginClass) {
        Objects.requireNonNull(pluginClass, "pluginClass");
        plugins.remove(pluginClass);
    }

    /** Searches for the plugin containers that provide plugin extensions of given type. */
    public Collection<PluginBox> findPluginsThatProvide(Class<? extends Extension> extensionType) {
        Objects.requireNonNull(extensionType, "extensionType");
        return plugins.values().stream()
                .filter(container -> container.providesExtensionsOfType(extensionType))
                .collect(Collectors.toList());
    }

    /** Searches for the plugin container that provides this specific extension implementation. */
    public Optional<PluginBox> whatPluginProvides(Class<? extends Extension> implClass) {
        Objects.requireNonNull(implClass, "implClass");
        return plugins.values().stream()
                .filter(pluginBox -> pluginBox.providesExtensionImpl(implClass))
                .findFirst();
    }
}
