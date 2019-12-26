package corf.base.plugin.internal;

import corf.base.plugin.Extension;
import corf.base.plugin.Includes;
import corf.base.plugin.Plugin;
import corf.base.plugin.Tool;
import org.apache.commons.lang3.ClassUtils;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

final class PluginLoader {

    private static final System.Logger LOGGER = System.getLogger(PluginLoader.class.getName());
    private static final Set<Class<? extends Extension>> SUPPORTED_EXTENSION_TYPES = Set.of(Tool.class);

    /** Default constructor. */
    public PluginLoader() { }

    /**
     * Scans given paths for the {@link Plugin} implementations, loads them via
     * {@link ServiceLoader} mechanism and returns loaded classes as iterable list.
     */
    public Iterable<Plugin> load(Set<Path> scanPaths) {
        Objects.requireNonNull(scanPaths, "scanPaths");
        LOGGER.log(INFO, MessageFormat.format("Scanning {0} for plugins", scanPaths));

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

    /**
     * Returns extension classes provided by specified plugin:
     * <ul>
     * <li>key - extension interface (Tool)</li>
     * <li>value - the list of classes which implement the interface</li>
     * </ul>
     * All data is extracted from the {@link corf.base.plugin.Includes} annotation.
     */
    public static Map<Class<? extends Extension>, Set<Class<? extends Extension>>> resolveExtensionTypes(
            Class<? extends Plugin> pluginClass) {

        Objects.requireNonNull(pluginClass, "pluginClass");

        Includes includes = pluginClass.getAnnotation(Includes.class);
        if (includes == null) {
            return Collections.emptyMap();
        }

        var extensionsMap = new HashMap<Class<? extends Extension>, Set<Class<? extends Extension>>>();

        for (Class<? extends Extension> extensionClass : includes.value()) {
            // this is somewhat redundant because PluginBox only accept instance objects
            if (Modifier.isAbstract(extensionClass.getModifiers()) || extensionClass.isInterface()) {
                LOGGER.log(WARNING, "Invalid extension type: " + ClassUtils.getCanonicalName(extensionClass) + ".");
                continue;
            }

            @SuppressWarnings({ "unchecked", "SimplifyStreamApiCallChains" })
            List<Class<? extends Extension>> implementedExtensionTypes = Arrays.stream(extensionClass.getInterfaces())
                    .filter(cls -> Extension.class.isAssignableFrom(cls) && SUPPORTED_EXTENSION_TYPES.contains(cls))
                    .map(cls -> (Class<? extends Extension>) cls)
                    .collect(Collectors.toList());

            if (implementedExtensionTypes.isEmpty()) {
                LOGGER.log(WARNING, "Extension doesn't implement any of the supported extension points: "
                        + ClassUtils.getCanonicalName(extensionClass) + ".");
                continue;
            }

            if (implementedExtensionTypes.size() > 1) {
                LOGGER.log(WARNING, "Extension must implement only one of the supported extension points: "
                        + ClassUtils.getCanonicalName(extensionClass) + ".");
                continue;
            }

            Class<? extends Extension> extensionType = implementedExtensionTypes.get(0);

            if (extensionsMap.containsKey(extensionType)) {
                extensionsMap.get(extensionType).add(extensionClass);
            } else {
                Set<Class<? extends Extension>> extensionClasses = new HashSet<>();
                extensionClasses.add(extensionClass);
                extensionsMap.put(extensionType, extensionClasses);
            }
        }

        return Collections.unmodifiableMap(extensionsMap);
    }
}
