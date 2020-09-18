package org.telekit.ui.service;

import de.skuzzle.semantic.Version;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.Messages;
import org.telekit.base.Settings;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.util.ZipUtils;
import org.telekit.ui.domain.PluginContainer;
import org.telekit.ui.domain.PluginContainer.Status;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.domain.TelekitException.fire;
import static org.telekit.base.util.CommonUtils.canonicalName;
import static org.telekit.base.util.FileUtils.*;
import static org.telekit.base.util.StringUtils.trimEquals;
import static org.telekit.ui.main.AllMessageKeys.*;

public class PluginManager {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());
    private static final Comparator<PluginContainer> ALPHABETICAL_COMPARATOR =
            Comparator.comparing(container -> container.getPlugin().getMetadata().getName());

    // plugins are loaded by different class loaders, but using
    // canonical class name as a key guarantees that the same plugin can't be loaded twice
    private Map<String, PluginContainer> loadedPlugins = new HashMap<>();

    public void loadPlugins(Set<String> disabledPlugins) {
        LOGGER.info("Found plugins:");

        Set<Path> scanPaths = Set.of(Settings.PLUGINS_DIR);
        ServiceLoader<Plugin> plugins = scanForPlugins(scanPaths);
        for (Plugin plugin : plugins) {
            Status status = !disabledPlugins.contains(canonicalName(plugin)) ?
                    Status.ENABLED :
                    Status.DISABLED;

            LOGGER.info(status + ": " + plugin.getMetadata().toString());
            registerPlugin(plugin, status);
        }
    }

    private void registerPlugin(Plugin plugin, Status status) {
        loadedPlugins.putIfAbsent(canonicalName(plugin), new PluginContainer(plugin, status));

        // MAYBE: validate plugins on load to prevent copy-paste installation method
        // validate(plugin);

        plugin.onLoad();
    }

    public void installFromZip(Path zipFile) {
        Path tempDir = null;
        try {
            if (!ZipUtils.isExtractable(zipFile)) {
                throw new TelekitException(Messages.get(PLUGMAN_MSG_FILE_IS_NOT_ZIP_ARCHIVE));
            }

            tempDir = Files.createTempDirectory("telekit_plugin_");

            ZipUtils.unzip(zipFile, tempDir);
            installFromDirectory(tempDir);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_EXTRACT_FILE), e);
        } finally {
            try {
                // Windows won't allow to delete loaded JAR file
                if (tempDir != null) {
                    new PluginCleaner().appendTask(tempDir);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void installFromDirectory(Path installDirectory) {
        List<Plugin> plugins = new ArrayList<>();
        scanForPlugins(Set.of(installDirectory))
                .iterator()
                .forEachRemaining(plugins::add);

        String installFailed = Messages.get(PLUGMAN_MSG_INSTALL_FAILED) + ": ";
        if (plugins.size() == 0) fire(installFailed + Messages.get(PLUGMAN_MSG_PATH_DOES_NOT_CONTAIN_PLUGINS));
        if (plugins.size() > 1) fire(installFailed + Messages.get(PLUGMAN_MSG_ONLY_ONE_PLUGIN_PER_DIR_ALLOWED));

        Plugin candidate = plugins.get(0);
        validate(candidate);

        // install
        URL location = candidate.getLocation();
        Path distribJAR = createFromURL(Objects.requireNonNull(location)).toPath();
        Path distribResources = installDirectory.resolve(Plugin.PLUGIN_RESOURCES_DIR);

        copyFile(distribJAR, Settings.PLUGINS_DIR.resolve(distribJAR.getFileName()), REPLACE_EXISTING);
        copyFolder(distribResources, Settings.getPluginDataDir(candidate.getClass()), false);

        registerPlugin(candidate, Status.INACTIVE);
    }

    private void validate(Plugin plugin) {
        final Metadata metadata = plugin.getMetadata();
        String installFailed = Messages.get(PLUGMAN_MSG_INSTALL_FAILED) + ": ";

        // check metadata
        if (metadata == null) fire(installFailed + Messages.get(PLUGMAN_MSG_MISSING_PLUGIN_METADATA));
        if (isBlank(metadata.getName())) fire(installFailed + Messages.get(PLUGMAN_MSG_INVALID_PLUGIN_NAME));
        if (isBlank(metadata.getVersion())) fire(installFailed + Messages.get(PLUGMAN_MSG_INVALID_PLUGIN_VERSION));

        // check minimal required version
        if (!isPluginMatchVersion(System.getProperty("application.version"), metadata.getRequiredVersion())) {
            fire(installFailed + Messages.get(PLUGMAN_MSG_REQUIRE_HIGHER_VERSION, metadata.getRequiredVersion()));
        }

        // check for duplicates
        if (find(plugin.getClass()) != null) fire(installFailed + Messages.get(PLUGMAN_MSG_PLUGIN_ALREADY_INSTALLED));
        boolean nameAlreadyUsed = loadedPlugins.values().stream()
                .map(PluginContainer::getPlugin)
                .map(Plugin::getMetadata)
                .anyMatch(elem -> trimEquals(elem.getName(), trim(metadata.getName())));
        if (nameAlreadyUsed) fire(installFailed + Messages.get(PLUGMAN_MSG_PLUGIN_SAME_NAME_ALREADY_INSTALLED));
    }

    private boolean isPluginMatchVersion(String appVersion, String pluginVersion) {
        if (isBlank(pluginVersion)) return true; // bypass check, any version supported

        Version actualVersion = Version.parseVersion(appVersion);
        Version requiredVersion = Version.parseVersion(pluginVersion);

        return actualVersion.withPreRelease("")
                .withBuildMetaData("")
                .compareTo(requiredVersion) >= 0;
    }

    public void uninstall(Class<? extends Plugin> clazz, boolean deleteResources) {
        PluginContainer container = find(clazz);
        if (container == null || container.getStatus() == Status.UNINSTALLED) return;

        Plugin plugin = container.getPlugin();
        container.setStatus(Status.UNINSTALLED);

        URL location = plugin.getLocation();
        Path pluginJAR = createFromURL(Objects.requireNonNull(location)).toPath();

        PluginCleaner cleaner = new PluginCleaner();
        cleaner.appendTask(pluginJAR);
        if (deleteResources) cleaner.appendTask(Settings.getPluginDataDir(clazz));
    }

    private ServiceLoader<Plugin> scanForPlugins(Set<Path> scanPaths) {
        ModuleLayer currentModuleLayer = getClass().getModule().getLayer();
        ModuleFinder moduleFinder = ModuleFinder.of(scanPaths.toArray(new Path[0]));
        ModuleFinder emptyFinder = ModuleFinder.of();
        Set<String> moduleNames = moduleFinder.findAll().stream()
                .map(moduleRef -> moduleRef.descriptor().name())
                .collect(Collectors.toSet());

        ClassLoader parentLoader = getClass().getClassLoader();
        Configuration configuration = currentModuleLayer
                .configuration()
                .resolveAndBind(moduleFinder, emptyFinder, moduleNames);
        ModuleLayer moduleLayer = currentModuleLayer
                .defineModulesWithManyLoaders(configuration, parentLoader);

        return ServiceLoader.load(moduleLayer, Plugin.class);
    }

    public List<PluginContainer> getAllPlugins() {
        return loadedPlugins.values().stream()
                .sorted(ALPHABETICAL_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<PluginContainer> getPlugins(Set<Status> statuses) {
        return getPlugins(statuses::contains);
    }

    public List<PluginContainer> getPlugins(Predicate<Status> predicate) {
        return loadedPlugins.values().stream()
                .filter(container -> predicate.test(container.getStatus()))
                .sorted(ALPHABETICAL_COMPARATOR)
                .collect(Collectors.toList());
    }

    public void setStatus(Set<String> classNames, Status status) {
        loadedPlugins.entrySet().stream()
                .filter(entry -> classNames.contains(entry.getKey()))
                .forEach(entry -> entry.getValue().setStatus(status));
    }

    @Nullable
    public PluginContainer find(Class<? extends Plugin> clazz) {
        return loadedPlugins.get(clazz.getCanonicalName());
    }

    @Nullable
    public PluginContainer findByClassName(String className) {
        return loadedPlugins.get(className);
    }
}
