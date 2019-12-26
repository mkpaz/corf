package corf.base.plugin;

import java.nio.file.Path;

/** Artifact repository implementation is responsible for plugin updates. */
public interface ArtifactRepository {

    /**
     * Returns the latest version number, which is used to check whether plugin
     * needs to be updated or not. Version number MUST follow semantic versioning.
     */
    String getLatestVersion();

    /**
     * Returns path to the latest plugin release. Thus, implementation is responsible
     * to download plugin artifact on local system, if necessary.
     */
    Path getLatestRelease();
}
