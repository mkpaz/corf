package org.telekit.base.plugin;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

public interface Plugin {

    String METADATA_NAME = "metadata.name";
    String METADATA_AUTHOR = "metadata.author";
    String METADATA_VERSION = "metadata.version";
    String METADATA_DESCRIPTION = "metadata.description";
    String METADATA_HOMEPAGE = "metadata.homepage";
    String METADATA_PLATFORM_VERSION = "metadata.platform-version";

    Metadata getMetadata();

    Collection<? extends DependencyModule> getModules();

    ResourceBundle getBundle(Locale locale);

    void start();

    void stop();

    boolean providesDocs();

    void openDocs(Locale locale);

    default @Nullable URL getLocation() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        return codeSource != null ? codeSource.getLocation() : null;
    }
}
