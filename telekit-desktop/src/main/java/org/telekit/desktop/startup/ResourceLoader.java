package org.telekit.desktop.startup;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ResourceLoader {

    private static final Logger LOG = Logger.getLogger(ResourceLoader.class.getName());

    private final Class<?> clazz;

    public ResourceLoader(Class<?> clazz) {
        this.clazz = clazz;
    }

    // Try to obtain specified resources, warn if not found.
    public Set<String> resolve(Collection<String> resources) {
        return getResources(resources, path ->
                LOG.warning("Unable to get resource at '" + path + "'")
        );
    }

    // Try to obtain specified resources, generate exception if not found.
    public Set<String> require(String... resources) {
        return getResources(Arrays.asList(resources), path -> {
            throw new RuntimeException("Unable to get resource at '" + path + "'");
        });
    }

    private Set<String> getResources(Collection<String> resources, Consumer<String> failureHandler) {
        if (resources.isEmpty()) { return Collections.emptySet(); }

        Set<String> result = new TreeSet<>();
        for (String path : resources) {
            URL url = getResource(path);
            if (url == null) {
                failureHandler.accept(path);
                continue;
            }

            result.add(url.toExternalForm());
        }

        return result;
    }

    private @Nullable URL getResource(String path) {
        return clazz.getResource(path);
    }
}
