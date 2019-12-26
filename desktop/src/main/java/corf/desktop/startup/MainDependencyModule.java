package corf.desktop.startup;

import backbonefx.di.Provides;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.inject.Singleton;
import corf.base.desktop.Overlay;
import corf.base.desktop.controls.DrawerPane;
import corf.base.io.JacksonMappers;
import corf.base.plugin.DependencyModule;
import corf.base.plugin.internal.PluginManager;
import corf.base.preferences.CompletionRegistry;
import corf.base.preferences.SharedPreferences;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.desktop.layout.MainStage;
import corf.desktop.service.DefaultSharedPreferences;
import corf.desktop.service.FileCompletionMonitoringService;
import corf.desktop.service.ToolRegistry;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public final class MainDependencyModule implements DependencyModule {

    private final MainStage mainStage;
    private final PreferencesConfig preferencesConfig;
    private final PluginConfig pluginConfig;
    private final ServicesConfig servicesConfig;
    private final LogConfig logConfig;

    public MainDependencyModule(MainStage mainStage,
                                PreferencesConfig preferencesConfig,
                                PluginConfig pluginConfig,
                                ServicesConfig servicesConfig,
                                LogConfig logConfig) {
        this.mainStage = Objects.requireNonNull(mainStage);
        this.preferencesConfig = Objects.requireNonNull(preferencesConfig);
        this.pluginConfig = Objects.requireNonNull(pluginConfig);
        this.servicesConfig = Objects.requireNonNull(servicesConfig);
        this.logConfig = Objects.requireNonNull(logConfig);
    }

    @Provides
    @Singleton
    public MainStage mainStage() {
        return mainStage;
    }

    @Provides
    @Singleton
    public LogConfig logConfig() {
        return logConfig;
    }

    @Provides
    @Singleton
    public ApplicationPreferences preferences() {
        return preferencesConfig.getPreferences();
    }

    @Provides
    @Singleton
    public SharedPreferences sharedPreferences() {
        return new DefaultSharedPreferences(preferencesConfig.getPreferences());
    }

    @Provides
    @Singleton
    public PluginManager pluginManager() {
        return pluginConfig.getPluginManager();
    }

    @Provides
    @Singleton
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    @Provides
    @Singleton
    public Overlay overlay() {
        return new DrawerPane();
    }

    @Provides
    @Singleton
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Provides
    @Singleton
    public CompletionRegistry completionRegistry() {
        return servicesConfig.getCompletionRegistry();
    }

    @Provides
    @Singleton
    public FileCompletionMonitoringService fileCompletionMonitoringService() {
        return servicesConfig.getCompletionMonitoringService();
    }

    @Provides
    @Singleton
    public XmlMapper xmlMapper() {
        return JacksonMappers.createXmlMapper();
    }

    @Provides
    @Singleton
    public YAMLMapper yamlMapper() {
        return JacksonMappers.createYamlMapper();
    }
}
