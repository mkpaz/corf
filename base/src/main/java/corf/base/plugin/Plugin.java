package corf.base.plugin;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;

/** Base interface for all application plugins. */
public interface Plugin {

    String METADATA_NAME = "metadata.name";
    String METADATA_AUTHOR = "metadata.author";
    String METADATA_VERSION = "metadata.version";
    String METADATA_DESCRIPTION = "metadata.description";
    String METADATA_HOMEPAGE = "metadata.homepage";
    String METADATA_PLATFORM_VERSION = "metadata.platform-version";

    /** Returns plugin metadata, such as name, author, version etc. */
    Metadata getMetadata();

    /** Returns plugin icon that will be shown in app plugin manager. */
    @Nullable Image getIcon();

    /** Returns plugin dependency injection configuration. */
    Collection<? extends DependencyModule> getModules();

    /** Returns an artifact repository to manage plugin updates. */
    @Nullable ArtifactRepository getRepository();

    /** The method will be executed when before plugin transition to the STARTED state. */
    void start();

    /** The method will be executed when before plugin transition to the STOPPED state. */
    void stop();

    /** Internal utility method that returns the path to the plugin. */
    default @Nullable URL getLocation() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        return codeSource != null ? codeSource.getLocation() : null;
    }
}
