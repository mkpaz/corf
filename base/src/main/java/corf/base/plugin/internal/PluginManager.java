package corf.base.plugin.internal;

import backbonefx.event.DefaultEventBus;
import backbonefx.event.EventBus;
import corf.base.Env;
import corf.base.Injector;
import corf.base.plugin.Extension;
import corf.base.plugin.Plugin;
import corf.base.preferences.internal.ApplicationPreferences;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static corf.base.i18n.I18n.t;
import static corf.base.i18n.M.*;
import static corf.base.plugin.internal.PluginState.*;
import static java.lang.System.Logger.Level.DEBUG;

public class PluginManager {

    private static final System.Logger LOGGER = System.getLogger(PluginManager.class.getName());

    private static final Comparator<PluginBox> ALPHABETICAL_COMPARATOR =
            Comparator.comparing(box -> box.getPlugin().getMetadata().getName());

    private final PluginRepository pluginRepository;
    private final PluginLoader pluginLoader;
    private final PluginInstaller pluginInstaller;
    private final ApplicationPreferences preferences;
    private final EventBus eventBus = new DefaultEventBus();

    public PluginManager(ApplicationPreferences preferences) {
        this.pluginLoader = new PluginLoader();
        this.pluginRepository = new PluginRepository();
        this.pluginInstaller = new PluginInstaller(pluginRepository, pluginLoader);
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }

    /**
     * Scans the plugin directory ({@link Env#PLUGINS_DIR}) and loads all found plugins.
     * It then checks all plugin class names and if the plugin name is not listed in the
     * {@link ApplicationPreferences#getDisabledPlugins()} it will set plugin state to the
     * {@link PluginState#LOADED}, otherwise plugin state will be {@link PluginState#DISABLED}.
     * Disabled plugins won't be started.
     */
    public void loadAllPlugins() {
        LOGGER.log(System.Logger.Level.INFO, String.format("Searching %s for plugins", Env.PLUGINS_DIR));

        Set<Path> scanPath;
        try (var fileStream = Files.walk(Env.PLUGINS_DIR)) {
            scanPath = fileStream
                    .filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".jar"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            LOGGER.log(System.Logger.Level.ERROR, "Error while scanning plugins directory.");
            throw new RuntimeException(e);
        }

        Iterable<Plugin> plugins = pluginLoader.load(scanPath);
        Set<String> disabledPlugins = preferences.getDisabledPlugins();

        for (var plugin : plugins) {
            var state = !disabledPlugins.contains(ClassUtils.getCanonicalName(plugin)) ? LOADED : DISABLED;
            var pluginBox = new PluginBox(plugin, state);
            LOGGER.log(System.Logger.Level.INFO, state + ": " + plugin.getMetadata().toString());
            pluginRepository.put(pluginBox);
        }
    }

    /**
     * Starts all registered plugins that have {@link PluginState#LOADED} state.
     * This is method is called only once at application startup. To restart individual plugin
     * it should be disabled and then enabled manually.
     * <ul>
     * <li>If any errors occurs while starting a plugin it will be moved to the {@link PluginState#FAILED}
     * state.</li>
     * <li>If one or more plugins were failed to start {@link PluginException} will be thrown.</li>
     * </ul>
     */
    public void startAllPlugins() throws PluginException {
        List<PluginBox> loadedPlugins = pluginRepository.find(pluginBox -> pluginBox.getState() == LOADED);
        if (loadedPlugins.isEmpty()) { return; }

        LOGGER.log(DEBUG, "Starting plugins");
        boolean errorFlag = false;

        for (var pluginBox : loadedPlugins) {
            try {
                startPlugin(pluginBox);
            } catch (PluginException e) {
                errorFlag = true;
            }
        }

        if (errorFlag) {
            throw new PluginException(PLUGIN_MSG_SOME_PLUGINS_WERE_NOT_STARTED);
        }
    }

    /**
     * Stops all registered plugins that have {@link PluginState#STARTED} state.
     * This is method is called only once at application shutdown.
     * <ul>
     * <li>If any errors occurs while stopping plugin it will be moved to {@link PluginState#FAILED} state.</li>
     * <li>If one or more plugins were failed to start {@link PluginException} will be thrown.</li>
     * </ul>
     */
    public void stopAllPlugins() throws PluginException {
        List<PluginBox> startedPlugins = pluginRepository.find(pluginBox -> pluginBox.getState() == STARTED);
        if (startedPlugins.isEmpty()) { return; }

        LOGGER.log(DEBUG, "Stopping plugins");
        boolean errorFlag = false;

        for (var pluginBox : startedPlugins) {
            try {
                stopPlugin(pluginBox);
            } catch (PluginException e) {
                errorFlag = true;
            }
        }

        if (errorFlag) {
            throw new PluginException(PLUGIN_MSG_SOME_PLUGINS_WERE_NOT_STOPPED);
        }
    }

    /** Returns all the plugins registered in plugins repository regardless of the {@link PluginState}. */
    public List<PluginBox> getAllPlugins() {
        List<PluginBox> plugins = pluginRepository.findAll();
        plugins.sort(ALPHABETICAL_COMPARATOR);
        return plugins;
    }

    /** Searches for the registered plugin with given class name. */
    public Optional<PluginBox> find(Class<? extends Plugin> pluginClass) {
        return pluginRepository.get(pluginClass);
    }

    /**
     * Installs plugin from specified path which can be archive file or directory.
     *
     * @param sourcePath archive file or directory
     * @see PluginInstaller#install(Path)
     */
    public PluginBox installPlugin(Path sourcePath) {
        Objects.requireNonNull(sourcePath, "sourcePath");
        return pluginInstaller.install(sourcePath);
    }

    /**
     * Uninstalls the specified plugin and (optionally) deletes all its resources
     * such as configuration, logs or any other user data from the file system.
     *
     * @see PluginInstaller#uninstall(Class, boolean)
     */
    public void uninstallPlugin(Class<? extends Plugin> pluginClass, boolean deleteResources) {
        Objects.requireNonNull(pluginClass, "pluginClass");

        Optional<PluginBox> pluginMaybe = pluginRepository.get(pluginClass);
        if (pluginMaybe.isEmpty()) { return; }

        var pluginBox = pluginMaybe.get();

        // it's always annoying when something that you want to uninstall
        // don't allow it for whatever reason, so uninstall is completely silent

        try {
            stopPlugin(pluginBox);
        } catch (PluginException ignored) { /* ignore */ }

        try {
            // notify about plugin stopped to reload UI
            eventBus.publish(new PluginStateEvent(pluginBox.getPluginClass(), STOPPED));

            pluginInstaller.uninstall(pluginClass, deleteResources);
        } catch (Throwable t) {
            LOGGER.log(System.Logger.Level.ERROR, "Error while deleting some plugin resources");
            LOGGER.log(System.Logger.Level.ERROR, ExceptionUtils.getStackTrace(t));
        }

        pluginBox.setState(UNINSTALLED);
    }

    /**
     * Enables plugin with given class name. Enabled plugin will be first set
     * to the {@link PluginState#LOADED} state and then started.
     */
    public void enablePlugin(Class<? extends Plugin> pluginClass) throws PluginException {
        Objects.requireNonNull(pluginClass, "pluginClass");

        Optional<PluginBox> pluginMaybe = pluginRepository.get(pluginClass);
        if (pluginMaybe.isEmpty()) { return; }

        var pluginBox = pluginMaybe.get();
        LOGGER.log(DEBUG, "Enabling plugin: " + ClassUtils.getCanonicalName(pluginBox.getPluginClass()));
        pluginBox.setState(LOADED);
        startPlugin(pluginBox);

        // notify about plugin started to reload UI
        eventBus.publish(new PluginStateEvent(pluginBox.getPluginClass(), STARTED));
    }

    /**
     * Disables plugin with given class name. Disabled plugin will be first stopped
     * and then set to the {@link PluginState#DISABLED} state.
     */
    public void disablePlugin(Class<? extends Plugin> pluginClass) throws PluginException {
        Objects.requireNonNull(pluginClass, "pluginClass");

        Optional<PluginBox> pluginMaybe = pluginRepository.get(pluginClass);
        if (pluginMaybe.isEmpty()) { return; }

        var pluginBox = pluginMaybe.get();
        LOGGER.log(DEBUG, "Disabling plugin: " + ClassUtils.getCanonicalName(pluginBox.getPluginClass()));
        if (pluginBox.getState() == STARTED) {
            stopPlugin(pluginBox);
        }

        // notify about plugin stopped to reload UI
        eventBus.publish(new PluginStateEvent(pluginBox.getPluginClass(), STOPPED));

        pluginBox.setState(DISABLED);
    }

    /**
     * This is entry point for any application tools that support extension mechanism. This method
     * searches the repository of registered plugins for extensions classes of specified type and
     * instantiates them. Only plugins in {@link PluginState#STARTED} state can provide extensions.
     * <p>
     * Extension may need to use some services or want to be a singleton. To create an extension
     * {@link Injector} is used. If object is not singleton {@link Injector} it will be created
     * using no-arg constructor. This is how {@link backbonefx.di.Feather} works by default.
     */
    public List<ExtensionBox> getExtensionsOfType(Class<? extends Extension> extensionType) {
        LOGGER.log(DEBUG, "Querying extension of type: " + ClassUtils.getCanonicalName(extensionType));
        Collection<PluginBox> matchedPlugins = pluginRepository.findPluginsThatProvide(extensionType);
        if (matchedPlugins.isEmpty()) {
            return Collections.emptyList();
        }

        LOGGER.log(DEBUG, "Extension providers found: " + matchedPlugins);
        return matchedPlugins.stream()
                .filter(pluginBox -> pluginBox.getState() == STARTED)
                .map(pluginBox -> createExtensionsOfType(pluginBox, extensionType))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /** Adds event listener to be notified on every plugin state changed event. */
    public void addEventListener(Consumer<PluginStateEvent> listener) {
        eventBus.subscribe(PluginStateEvent.class, Objects.requireNonNull(listener));
    }

    private void startPlugin(PluginBox pluginBox) throws PluginException {
        try {
            LOGGER.log(DEBUG, "Starting plugin: " + ClassUtils.getCanonicalName(pluginBox.getPluginClass()));
            pluginBox.getPlugin().start();
            pluginBox.setState(STARTED);
        } catch (Throwable t) {
            LOGGER.log(System.Logger.Level.ERROR, "Failed to start plugin: " + ClassUtils.getCanonicalName(pluginBox.getPluginClass()));
            LOGGER.log(System.Logger.Level.ERROR, ExceptionUtils.getStackTrace(t));
            pluginBox.setState(PluginState.FAILED);
            throw new PluginException(t(PLUGIN_MSG_ERROR_WHILE_START), t);
        }
    }

    private void stopPlugin(PluginBox pluginBox) throws PluginException {
        try {
            LOGGER.log(DEBUG, "Stopping plugin: " + ClassUtils.getCanonicalName(pluginBox.getPluginClass()));
            pluginBox.getPlugin().stop();
            pluginBox.setState(STOPPED);
        } catch (Throwable t) {
            LOGGER.log(System.Logger.Level.ERROR, "Failed to stop plugin: " + ClassUtils.getCanonicalName(pluginBox.getPluginClass()));
            LOGGER.log(System.Logger.Level.ERROR, ExceptionUtils.getStackTrace(t));
            pluginBox.setState(PluginState.FAILED);
            throw new PluginException(t(PLUGIN_MSG_ERROR_WHILE_STOP), t);
        }
    }

    private Collection<ExtensionBox> createExtensionsOfType(PluginBox pluginBox,
                                                            Class<? extends Extension> extensionType) {
        Collection<Class<? extends Extension>> extensionClasses = pluginBox.getExtensionsOfType(extensionType);
        if (extensionClasses.isEmpty()) { return Collections.emptyList(); }

        var extensions = new ArrayList<ExtensionBox>();
        Injector injector = Injector.getInstance();

        for (Class<? extends Extension> cls : extensionClasses) {
            LOGGER.log(DEBUG, "Instantiating extension: " + ClassUtils.getCanonicalName(extensionType));
            // even if object is not provided by the injector (as a singleton)
            // it will be created via no-arg constructor, this is how getBean() method works
            Extension extension = injector.getBean(cls);
            extensions.add(new ExtensionBox(extension, pluginBox.getPluginClass()));
        }

        return Collections.unmodifiableList(extensions);
    }
}
