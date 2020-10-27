package org.telekit.base.plugin.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.Env;
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

import static org.telekit.base.plugin.Plugin.PLUGIN_DOCS_INDEX_FILE_PREFIX;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.base.util.FileUtils.findFilesByPrefix;
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

    public boolean hasStoredData() {
        Path pluginDataDir = Env.getPluginDataDir(plugin.getClass());
        return Files.exists(pluginDataDir) && isDirEmpty(pluginDataDir);
    }

    public @Nullable Path getPluginJarPath() {
        URL jarLocation = plugin.getLocation();
        if (jarLocation == null) return null;
        try {
            return Paths.get(jarLocation.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public @NotNull Collection<Path> getPluginDataPaths() {
        Path pluginDataDir = Env.getPluginDataDir(plugin.getClass());
        if (!Files.exists(pluginDataDir)) return Collections.emptyList();

        try {
            return Files.walk(pluginDataDir)
                    .filter(path -> !path.equals(pluginDataDir))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public boolean doesPluginProvideDocs() {
        Path pluginCodsPath = Env.getPluginDocsDir(getPluginClass());
        return !findFilesByPrefix(pluginCodsPath, PLUGIN_DOCS_INDEX_FILE_PREFIX).isEmpty();
    }

    public Optional<Path> getPluginDocsIndex(Locale locale) {
        Path pluginCodsPath = Env.getPluginDocsDir(getPluginClass());
        String i18nIndexFilePrefix = PLUGIN_DOCS_INDEX_FILE_PREFIX + "_" + locale.getLanguage();

        // first search for localized docs
        List<Path> foundDocs = findFilesByPrefix(pluginCodsPath, i18nIndexFilePrefix);
        if (!foundDocs.isEmpty()) return Optional.of(foundDocs.get(0));

        // then any other docs
        foundDocs = findFilesByPrefix(pluginCodsPath, PLUGIN_DOCS_INDEX_FILE_PREFIX);
        return !foundDocs.isEmpty() ? Optional.of(foundDocs.get(0)) : Optional.empty();
    }

    @Override
    public String toString() {
        return "PluginContainer{" +
                "plugin=" + className(getPluginClass()) +
                ", state=" + state +
                '}';
    }
}
