package org.telekit.desktop.startup;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.Env;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.routing.Router;
import org.telekit.base.di.DependencyModule;
import org.telekit.base.di.Provides;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.preferences.SharedPreferences;
import org.telekit.base.preferences.internal.ApplicationPreferences;
import org.telekit.base.preferences.internal.SecurityPreferences;
import org.telekit.base.preferences.internal.Vault;
import org.telekit.base.preferences.internal.VaultKeyProvider;
import org.telekit.base.service.completion.CompletionRegistry;
import org.telekit.base.service.crypto.DefaultEncryptionService;
import org.telekit.base.service.crypto.EncryptionService;
import org.telekit.base.service.crypto.Encryptor;
import org.telekit.base.service.crypto.KeyProvider;
import org.telekit.base.util.Mappers;
import org.telekit.controls.widgets.OverlayBase;
import org.telekit.desktop.service.DefaultSharedPreferences;
import org.telekit.desktop.service.FileCompletionMonitoringService;
import org.telekit.desktop.startup.config.*;
import org.telekit.desktop.views.MainStage;
import org.telekit.desktop.views.system.WelcomeView;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.telekit.base.preferences.internal.Vault.MASTER_KEY_ALIAS;

public final class MainDependencyModule implements DependencyModule {

    private final MainStage mainStage;
    private final PreferencesConfig preferencesConfig;
    private final SecurityConfig securityConfig;
    private final PluginConfig pluginConfig;
    private final ServicesConfig servicesConfig;
    private final LogConfig logConfig;

    public MainDependencyModule(MainStage mainStage,
                                PreferencesConfig preferencesConfig,
                                SecurityConfig securityConfig,
                                PluginConfig pluginConfig,
                                ServicesConfig servicesConfig,
                                LogConfig logConfig) {
        this.mainStage = mainStage;
        this.preferencesConfig = Objects.requireNonNull(preferencesConfig);
        this.securityConfig = Objects.requireNonNull(securityConfig);
        this.pluginConfig = Objects.requireNonNull(pluginConfig);
        this.servicesConfig = Objects.requireNonNull(servicesConfig);
        this.logConfig = Objects.requireNonNull(logConfig);
    }

    @Provides
    @Singleton
    public MainStage mainStage() { return mainStage; }

    @Provides
    @Singleton
    public ApplicationPreferences preferences() { return preferencesConfig.getPreferences(); }

    @Provides
    @Singleton
    public Vault vault() { return securityConfig.getVault(); }

    @Provides
    @Singleton
    public PluginManager pluginManager() { return pluginConfig.getPluginManager(); }

    @Provides
    @Singleton
    public Router router() {
        Router router = new Router();
        router.registerRoute(WelcomeView.ROUTE.getName(), WelcomeView.class);
        Config.getBuiltinTools().forEach(tool -> router.registerRoute(tool.id(), tool.getComponent()));
        return router;
    }

    @Provides
    @Singleton
    public Overlay overlay() {
        return new OverlayBase();
    }

    @Provides
    @Singleton
    @Named(Env.MASTER_ENC_SERVICE_QUALIFIER)
    public EncryptionService masterEncryptionService() {
        SecurityPreferences securityPreferences = preferences().getSecurityPreferences();
        KeyProvider keyProvider = new VaultKeyProvider(vault(), securityPreferences, MASTER_KEY_ALIAS);
        Encryptor encryptor = Encryptor.createEncryptor(Env.DEFAULT_ENCRYPTION_ALG);
        return new DefaultEncryptionService(encryptor, keyProvider);
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
        return Mappers.createXmlMapper();
    }

    @Provides
    @Singleton
    public YAMLMapper yamlMapper() {
        return Mappers.createYamlMapper();
    }

    @Provides
    @Singleton
    public SharedPreferences sharedPreferences() {
        return new DefaultSharedPreferences(preferencesConfig.getPreferences());
    }

    @Provides
    @Singleton
    public LogConfig logConfig() {
        return logConfig;
    }
}
