package org.telekit.base.plugin;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.security.CodeSource;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public interface Plugin {

    String PLUGIN_DATA_DIR_NAME = "data";
    String PLUGIN_DOCS_DIR_NAME = "docs";
    String PLUGIN_DOCS_INDEX_FILE_PREFIX = "index";

    String METADATA_NAME = "metadata.name";
    String METADATA_AUTHOR = "metadata.author";
    String METADATA_VERSION = "metadata.version";
    String METADATA_DESCRIPTION = "metadata.description";
    String METADATA_HOMEPAGE = "metadata.homepage";
    String METADATA_PLATFORM_VERSION = "metadata.platform-version";

    Metadata getMetadata();

    List<? extends DependencyModule> getModules();

    ResourceBundle getBundle(Locale locale);

    void start();

    void stop();

    default @Nullable URL getLocation() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        return codeSource != null ? codeSource.getLocation() : null;
    }
}
