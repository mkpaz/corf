package org.telekit.ui;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.CompletionRegistry;
import org.telekit.base.Env;
import org.telekit.base.domain.Proxy;
import org.telekit.base.feather.Provides;
import org.telekit.base.plugin.DependencyModule;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.preferences.Security;
import org.telekit.base.preferences.Vault;
import org.telekit.base.preferences.VaultKeyProvider;
import org.telekit.base.service.CompletionProvider;
import org.telekit.base.service.EncryptionService;
import org.telekit.base.service.Encryptor;
import org.telekit.base.service.KeyProvider;
import org.telekit.base.service.impl.DefaultEncryptionService;
import org.telekit.base.service.impl.KeyValueCompletionProvider;
import org.telekit.base.util.Mappers;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.telekit.base.preferences.Vault.MASTER_KEY_ALIAS;
import static org.telekit.base.util.FileUtils.getFileName;

public final class MainDependencyModule implements DependencyModule {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private final PluginManager pluginManager;
    private final ApplicationPreferences preferences;
    private final Vault vault;

    public MainDependencyModule(ApplicationPreferences preferences,
                                PluginManager pluginManager,
                                Vault vault) {
        this.pluginManager = pluginManager;
        this.preferences = preferences;
        this.vault = vault;
    }

    /* Singletons */

    @Provides
    @Singleton
    public ApplicationPreferences applicationPreferences() {
        return preferences;
    }

    @Provides
    @Singleton
    public Vault vault() {
        return vault;
    }

    @Provides
    @Singleton
    public PluginManager pluginManager() {
        return pluginManager;
    }

    @Provides
    @Singleton
    @Named("masterEncryptionService")
    public EncryptionService masterEncryptionService() {
        Security securityPrefs = preferences.getSecurity();
        KeyProvider keyProvider = new VaultKeyProvider(vault, securityPrefs, MASTER_KEY_ALIAS);
        Encryptor encryptor = Encryptor.createEncryptor(Env.DEFAULT_ENCRYPTION_ALG);
        return new DefaultEncryptionService(encryptor, keyProvider);
    }

    @Provides
    @Singleton
    public CompletionRegistry completionRegistry() {
        CompletionRegistry registry = new CompletionRegistry();
        findCompletionProviders().forEach(registry::registerProvider);
        return registry;
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

    /* Providers */

    @Provides
    public Proxy proxy() {
        return new Proxy(applicationPreferences().getProxy());
    }

    ///////////////////////////////////////////////////////////////////////////

    // TODO: monitor directory for new files
    public @NotNull List<CompletionProvider<?>> findCompletionProviders() {
        List<CompletionProvider<?>> providers = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Env.AUTOCOMPLETE_DIR)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) continue;
                if (entry.getFileName().toString().endsWith(".properties")) {
                    providers.add(new KeyValueCompletionProvider(getFileName(entry), entry));
                }
            }
        } catch (IOException e) {
            LOGGER.severe(ExceptionUtils.getStackTrace(e));
        }
        return providers;
    }
}
