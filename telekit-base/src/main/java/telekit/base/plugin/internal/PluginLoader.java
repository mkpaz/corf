package telekit.base.plugin.internal;

import telekit.base.plugin.Extension;
import telekit.base.plugin.Includes;
import telekit.base.plugin.Plugin;
import telekit.base.plugin.Tool;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ClassUtils.getCanonicalName;

public class PluginLoader {

    private static final Logger LOGGER = Logger.getLogger(PluginLoader.class.getName());

    public static final Set<Class<? extends Extension>> SUPPORTED_EXTENSION_TYPES = Set.of(
            Tool.class
    );

    public Iterable<Plugin> load(Set<Path> scanPaths) {
        LOGGER.info(MessageFormat.format("Scanning {0} for plugins", scanPaths));

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
     * <li>key - extension interface (Tool...);</li>
     * <li>value - list of classes which implement that interface.</li>
     * </ul>
     * All data is extracted from {@link telekit.base.plugin.Includes} annotation.
     */
    public static Map<Class<? extends Extension>, Set<Class<? extends Extension>>> resolveExtensionTypes(
            Class<? extends Plugin> pluginClass
    ) {
        Includes includes = pluginClass.getAnnotation(Includes.class);
        if (includes == null) { return Collections.emptyMap(); }

        Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionsMap = new HashMap<>();

        for (Class<? extends Extension> extensionClass : includes.value()) {
            // this is somewhat redundant because PluginBox only accept instance objects
            if (Modifier.isAbstract(extensionClass.getModifiers()) || extensionClass.isInterface()) {
                LOGGER.warning("Invalid extension type: " + getCanonicalName(extensionClass));
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Class<? extends Extension>> implementedExtensionTypes = Arrays.stream(extensionClass.getInterfaces())
                    .filter(cls -> Extension.class.isAssignableFrom(cls) && SUPPORTED_EXTENSION_TYPES.contains(cls))
                    .map(cls -> (Class<? extends Extension>) cls)
                    .collect(Collectors.toList());

            if (implementedExtensionTypes.isEmpty()) {
                LOGGER.warning("Extension doesn't implement any of the supported extension points: " + getCanonicalName(extensionClass));
                continue;
            }

            if (implementedExtensionTypes.size() > 1) {
                LOGGER.warning("Extension must implement only one of the supported extension points: " + getCanonicalName(extensionClass));
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
