package org.telekit.base.plugin;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.security.CodeSource;
import java.util.List;

public interface Plugin {

    String PLUGIN_RESOURCES_DIR = "resources";

    String METADATA_NAME = "metadata.name";
    String METADATA_AUTHOR = "metadata.author";
    String METADATA_VERSION = "metadata.version";
    String METADATA_DESCRIPTION = "metadata.description";
    String METADATA_HOMEPAGE = "metadata.homepage";
    String METADATA_REQUIRES_VERSION = "metadata.requires.version";

    Metadata getMetadata();

    List<? extends DependencyModule> getModules();

    List<Tool> getTools();

    void onLoad();

    void openDocs();

    boolean providesDocs();

    @Nullable
    default URL getLocation() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        return codeSource != null ? codeSource.getLocation() : null;
    }
}
