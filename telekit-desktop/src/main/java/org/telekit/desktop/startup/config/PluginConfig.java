package org.telekit.desktop.startup.config;

import org.telekit.base.di.DependencyModule;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.internal.PluginBox;
import org.telekit.base.plugin.internal.PluginCleaner;
import org.telekit.base.plugin.internal.PluginException;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.preferences.internal.ApplicationPreferences;
import org.telekit.desktop.startup.ResourceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public final class PluginConfig implements Config {

    private final ApplicationPreferences preferences;
    private PluginManager pluginManager;

    public PluginConfig(ApplicationPreferences preferences) {
        this.preferences = preferences;
        initialize();
    }

    private void initialize() {
        cleanupUninstalledPlugins();
        loadPlugins();
    }

    private void cleanupUninstalledPlugins() {
        PluginCleaner cleaner = new PluginCleaner();
        cleaner.executeAllSilently();
    }

    private void loadPlugins() {
        pluginManager = new PluginManager(preferences);
        pluginManager.loadAllPlugins();
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public List<DependencyModule> getDependencyModules() {
        List<DependencyModule> modules = new ArrayList<>();
        for (PluginBox container : pluginManager.getAllPlugins()) {
            Plugin plugin = container.getPlugin();
            Collection<? extends DependencyModule> pluginModules = plugin.getModules();
            if (isNotEmpty(pluginModules)) { modules.addAll(pluginModules); }
        }
        return modules;
    }

    public List<String> getStylesheets() {
        List<String> result = new ArrayList<>();
        for (PluginBox container : pluginManager.getAllPlugins()) {
            Collection<String> pluginsStyles = container.getPlugin().getStylesheets();
            if (isEmpty(pluginsStyles)) { continue; }

            ResourceLoader loader = new ResourceLoader(container.getPluginClass());
            result.addAll(loader.resolve(pluginsStyles));
        }
        return result;
    }

    public void startPlugins() {
        try {
            // TODO: Notify user if some plugins weren't started
            // NOTE: plugins should be started BEFORE MainController initialization
            //       because it queries extensions to build-up menu bar
            pluginManager.startAllPlugins();
        } catch (PluginException ignored) {
            // even if some plugin wasn't started, it shouldn't prevent application
            // from loading because it may work without plugins
        }
    }
}
