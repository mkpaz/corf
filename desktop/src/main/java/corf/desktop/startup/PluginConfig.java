package corf.desktop.startup;

import org.apache.commons.collections4.CollectionUtils;
import corf.base.plugin.DependencyModule;
import corf.base.plugin.Plugin;
import corf.base.plugin.internal.PluginCleaner;
import corf.base.plugin.internal.PluginException;
import corf.base.plugin.internal.PluginManager;
import corf.base.preferences.internal.ApplicationPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PluginConfig implements Config {

    private final PluginManager pluginManager;

    public PluginConfig(ApplicationPreferences preferences) {
        this.pluginManager = new PluginManager(Objects.requireNonNull(preferences));
        init();
    }

    private void init() {
        cleanupUninstalledPlugins();
        pluginManager.loadAllPlugins();
    }

    private void cleanupUninstalledPlugins() {
        PluginCleaner cleaner = new PluginCleaner();
        cleaner.executeAllSilently();
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public List<DependencyModule> getDependencyModules() {
        var modules = new ArrayList<DependencyModule>();
        for (var container : pluginManager.getAllPlugins()) {
            Plugin plugin = container.getPlugin();
            Collection<? extends DependencyModule> pluginModules = plugin.getModules();
            if (CollectionUtils.isNotEmpty(pluginModules)) {
                modules.addAll(pluginModules);
            }
        }
        return modules;
    }

    public void startPlugins() {
        try {
            pluginManager.startAllPlugins();
        } catch (PluginException ignored) {
            // even if some plugins weren't started, it shouldn't prevent application from loading
        }
    }
}
