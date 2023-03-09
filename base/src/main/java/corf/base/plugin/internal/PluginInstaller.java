package corf.base.plugin.internal;

import corf.base.Env;
import corf.base.exception.AppException;
import corf.base.io.FileSystemUtils;
import corf.base.io.ZipUtils;
import corf.base.plugin.Plugin;
import de.skuzzle.semantic.Version;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static corf.base.i18n.I18n.t;
import static corf.base.i18n.M.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

final class PluginInstaller {

    private static final System.Logger LOGGER = System.getLogger(PluginManager.class.getName());
    private static final String[] supportedExtensions = { ".zip" };

    private final PluginRepository pluginRepository;
    private final PluginLoader pluginLoader;
    private final PluginCleaner pluginCleaner;

    public PluginInstaller(PluginRepository pluginRepository,
                           PluginLoader pluginLoader) {
        this.pluginRepository = Objects.requireNonNull(pluginRepository, "pluginRepository");
        this.pluginLoader = Objects.requireNonNull(pluginLoader, "pluginLoader");
        this.pluginCleaner = new PluginCleaner();
    }

    /**
     * Installs plugin from the given file system path.
     * Plugins are supposed to be distributed as archive files (for now only ZIP format is supported)
     * that follow the specific directory layout. This method accepts path to such directory
     * as well, so you can use unpacked (or not yet packed) plugin artifacts.
     *
     * @see #installFromDirectory(Path)
     */
    public PluginBox install(Path sourcePath) {
        Objects.requireNonNull(sourcePath, "sourcePath");

        Path installDir = null;
        boolean needCleanup = false;

        LOGGER.log(INFO, "Trying to install plugin from " + sourcePath.toAbsolutePath());
        if (Files.isRegularFile(sourcePath)
                && StringUtils.endsWithAny(sourcePath.toString().toLowerCase(), supportedExtensions)) {
            installDir = extractToTempDir(sourcePath);
            needCleanup = true;
        } else if (Files.isDirectory(sourcePath)) {
            installDir = sourcePath;
        } else {
            fireInstallFailed(PLUGIN_MSG_PATH_DOES_NOT_CONTAIN_PLUGINS);
        }

        Objects.requireNonNull(installDir, "Unable to determine installation directory.");

        try {
            return installFromDirectory(installDir);
        } finally {
            // it's not really necessary to delete temp dir
            try {
                if (needCleanup) {
                    LOGGER.log(INFO, "Assigning cleanup tasks");
                    // use cleaner because Windows won't allow deleting loaded JAR file
                    pluginCleaner.appendTask(installDir);
                }
            } catch (Exception ignored) { /* ignore */ }
        }
    }

    /**
     * Uninstalls the specified plugin and (optionally) deletes all its resources
     * such as configuration, logs or any other user data from the file system.
     */
    public void uninstall(Class<? extends Plugin> pluginClass, boolean deleteResources) {
        Objects.requireNonNull(pluginClass, "pluginClass");

        Optional<PluginBox> pluginMaybe = pluginRepository.get(pluginClass);
        if (pluginMaybe.isEmpty() || pluginMaybe.get().getState() == PluginState.UNINSTALLED) { return; }

        LOGGER.log(INFO, "Uninstalling " + pluginClass);
        var pluginBox = pluginMaybe.get();
        var plugin = pluginBox.getPlugin();

        pluginBox.setState(PluginState.UNINSTALLED);

        var pluginDir = Env.getPluginDir(plugin.getClass());
        var pluginJarPath = Objects.requireNonNull(pluginBox.getJarPath(), "JAR file doe not exist.");
        var pluginLibPath = Env.getPluginLibDir(plugin.getClass());
        var pluginConfigPath = Env.getPluginConfigDir(plugin.getClass());
        LOGGER.log(INFO, pluginClass + " code resides in " + pluginJarPath.toAbsolutePath());
        LOGGER.log(INFO, pluginClass + " configs reside in " + pluginConfigPath.toAbsolutePath());

        var cleaner = new PluginCleaner();

        // delete lib
        if (pluginJarPath.startsWith(pluginLibPath)) {
            cleaner.appendTask(pluginLibPath);
        } else {
            // Handle the situation when user wants to immediately uninstall just installed
            // plugin without restart. After installation (but before restart) plugin is
            // loaded from JAR that located in temp directory. Thus, we need to explicitly
            // delete JAR copy from the plugin lib directory.
            deleteDirQuietly(pluginLibPath);

            // and we still have to remove temp file
            cleaner.appendTask(pluginJarPath);
        }

        // delete config dir, if empty (in the worst scenario it can still be locked)
        if (Files.exists(pluginConfigPath) && FileSystemUtils.isEmptyDir(pluginConfigPath)) {
            deleteDirQuietly(pluginConfigPath);
        }

        // delete everything, including config dir if it wasn't deleted earlier
        if (deleteResources) {
            cleaner.appendTask(pluginDir);
        }

        LOGGER.log(INFO, "Uninstallation finished. Some resources will be deleted on next application startup.");
    }

    /**
     * Checks whether given plugin can be installed or not. Basically it validates all
     * mandatory params and ensures that the plugin supports the current platform version.
     */
    public void validate(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        var meta = plugin.getMetadata();

        // check metadata
        if (meta == null) {
            fireInstallFailed(PLUGIN_MSG_INVALID_METADATA);
        }
        if (StringUtils.isBlank(meta.getName())) {
            fireInstallFailed(PLUGIN_MSG_INVALID_NAME);
        }
        if (StringUtils.isBlank(meta.getVersion())) {
            fireInstallFailed(PLUGIN_MSG_INVALID_VERSION);
        }

        // check required platform version
        if (!isSupportedPlatformVersion(meta.getPlatformVersion())) {
            fireInstallFailed(
                    PLUGIN_MSG_HIGHER_PLATFORM_VERSION_REQUIRED,
                    StringUtils.defaultString(meta.getPlatformVersion(), "null")
            );
        }

        // check for duplicates
        if (pluginRepository.contains(plugin.getClass())) {
            fireInstallFailed(PLUGIN_MSG_ALREADY_INSTALLED);
        }
    }

    /** Checks whether plugin platform version requirements matches current platform version. */
    private boolean isSupportedPlatformVersion(@Nullable String requiredPlatformVersion) {
        // bypass check, plugin has no version requirements
        if (StringUtils.isBlank(requiredPlatformVersion)) {
            return true;
        }
        if (!Version.isValidVersion(requiredPlatformVersion)) {
            fireInstallFailed(PLUGIN_MSG_INVALID_METADATA);
        }
        return Version.parseVersion(Objects.requireNonNull(Env.getAppVersion()))
                .compareTo(Version.parseVersion(requiredPlatformVersion)) >= 0;
    }

    /**
     * Installs unpacked plugin. For this to work, specific directory layout must be used.
     * <pre>
     * install_dir
     * ├── config
     * │   └── example.cfg
     * └── plugin.jar
     * </pre>
     * Everything but plugin JAR file is optional.
     */
    private PluginBox installFromDirectory(Path installDir) {
        Objects.requireNonNull(installDir, "installDir");

        var foundPlugins = new ArrayList<Plugin>();
        pluginLoader.load(Set.of(installDir))
                .iterator()
                .forEachRemaining(foundPlugins::add);

        LOGGER.log(INFO, "Installation begin");

        if (foundPlugins.isEmpty()) {
            fireInstallFailed(PLUGIN_MSG_PATH_DOES_NOT_CONTAIN_PLUGINS);
        }
        if (foundPlugins.size() > 1) {
            fireInstallFailed(PLUGIN_MSG_ONLY_ONE_PLUGIN_PER_DIR_ALLOWED);
        }

        var plugin = foundPlugins.get(0);
        if (plugin.getMetadata() != null) {
            LOGGER.log(INFO, plugin.getMetadata().toString());
        }
        validate(plugin);

        var location = plugin.getLocation();
        var sourceJarPath = FileUtils.toFile(Objects.requireNonNull(location)).toPath();
        var sourceConfigPath = installDir.resolve(Env.CONFIG_DIR_NAME);

        var pluginDir = Env.getPluginDir(plugin.getClass());
        var destLibPath = Env.getPluginLibDir(plugin.getClass());
        var destConfigPath = Env.getPluginConfigDir(plugin.getClass());

        // cancel cleanup tasks if plugin installed immediately
        // after uninstallation without the application restart
        var cleaner = new PluginCleaner();

        try {
            // create plugin root directory first
            FileSystemUtils.createDirTree(pluginDir);

            LOGGER.log(INFO, "Copying JAR file");
            if (Files.exists(sourceJarPath)) {
                FileSystemUtils.createDir(destLibPath);
                FileSystemUtils.copyFile(sourceJarPath, destLibPath.resolve(sourceJarPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                cleaner.cancelTask(destLibPath);
            }

            LOGGER.log(INFO, "Copying plugin resources");
            if (Files.exists(sourceConfigPath)) {
                FileSystemUtils.copyDir(sourceConfigPath, destConfigPath, false);
                cleaner.cancelTask(destConfigPath);
            }
        } catch (Throwable t) {
            LOGGER.log(ERROR, "Unable to copy plugin files to the application directory");
            LOGGER.log(ERROR, ExceptionUtils.getStackTrace(t));
            try {
                FileSystemUtils.deleteFile(pluginDir);
            } catch (Throwable ignored) { /* ignore */}

            fireInstallFailed(MSG_GENERIC_IO_ERROR);
        }

        var pluginBox = new PluginBox(plugin, PluginState.INSTALLED);
        pluginRepository.put(pluginBox);

        LOGGER.log(INFO, "Installation finished");

        return pluginBox;
    }

    private static Path extractToTempDir(Path archiveFile) {
        try {
            Path tempDir = Files.createTempDirectory(Env.APP_NAME.toLowerCase() + "_plugin_");
            LOGGER.log(INFO, "Extracting to " + tempDir.toAbsolutePath());

            if (!ZipUtils.isExtractable(archiveFile)) {
                throw new AppException(t(MGG_UNABLE_TO_EXTRACT_FILE));
            }
            ZipUtils.unzip(archiveFile, tempDir);

            return tempDir;
        } catch (IOException e) {
            throw new AppException(t(MGG_UNABLE_TO_EXTRACT_FILE), e);
        }
    }

    private void fireInstallFailed(String reason) {
        throw new AppException(t(PLUGIN_MSG_PREFIX_INSTALLATION_FAILED) + " " + t(reason));
    }

    private void fireInstallFailed(String reason, Object... args) {
        throw new AppException(t(PLUGIN_MSG_PREFIX_INSTALLATION_FAILED) + " " + t(reason, args));
    }

    private void deleteDirQuietly(Path dir) {
        try {
            FileSystemUtils.deleteDir(dir);
        } catch (Throwable ignored) { /* ignore */ }
    }
}
