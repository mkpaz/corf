package org.telekit.base.service;

import java.nio.file.Path;

public interface ArtifactRepository {

    /**
     * Returns latest version number, which is used to check whether plugin
     * needs to be updated or not. Version number must follow semantic versioning.
     */
    String getLatestVersion();

    /**
     * Returns path to the latest plugin release. Thus, implementation is responsible
     * to download plugin artifact on local system, if necessary.
     */
    Path getLatestRelease();
}
