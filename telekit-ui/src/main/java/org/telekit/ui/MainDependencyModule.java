package org.telekit.ui;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.feather.Provides;
import org.telekit.base.plugin.DependencyModule;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.preferences.ApplicationPreferences;

import javax.inject.Singleton;

public class MainDependencyModule implements DependencyModule {

    private final PluginManager pluginManager;
    private final ApplicationPreferences preferences;

    public MainDependencyModule(ApplicationPreferences preferences, PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.preferences = preferences;
    }

    @Provides
    @Singleton
    public PluginManager pluginManager() {
        return pluginManager;
    }

    @Provides
    @Singleton
    public ApplicationPreferences applicationPreferences() {
        return preferences;
    }

    @Provides
    @Singleton
    public XmlMapper xmlMapper() {
        return Mappers.createXmlMapper();
    }

    @Provides
    @Singleton
    public YAMLMapper yamlMapper() {
        return Mappers.createYamlMapper();
    }
}
