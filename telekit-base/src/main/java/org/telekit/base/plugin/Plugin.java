package org.telekit.base.plugin;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.di.DependencyModule;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.base.service.ArtifactRepository;
import org.telekit.base.service.EncryptionService;

import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Locale;

public interface Plugin {

    String METADATA_NAME = "metadata.name";
    String METADATA_AUTHOR = "metadata.author";
    String METADATA_VERSION = "metadata.version";
    String METADATA_DESCRIPTION = "metadata.description";
    String METADATA_HOMEPAGE = "metadata.homepage";
    String METADATA_PLATFORM_VERSION = "metadata.platform-version";

    Metadata getMetadata();

    Collection<? extends DependencyModule> getModules();

    BundleLoader getBundleLoader();

    Collection<String> getStylesheets();

    ArtifactRepository getRepository();

    void start();

    void stop();

    /**
     * Updates encrypted data on cipher key or algorithm change.
     * Should only be implemented if plugin uses application encryption services.
     */
    void updateEncryptedData(EncryptionService oldEncryptor, EncryptionService newEncryptor);

    boolean providesDocs();

    void openDocs(Locale locale);

    default @Nullable URL getLocation() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        return codeSource != null ? codeSource.getLocation() : null;
    }
}
