package corf.desktop;

import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/** A utility class to simplify obtaining classpath resources. */
public final class ResourceLoader {

    private static final System.Logger LOGGER = System.getLogger(ResourceLoader.class.getName());

    private final Class<?> anchorClass;

    public ResourceLoader(@Nullable Class<?> anchorClass) {
        this.anchorClass = Objects.requireNonNullElse(anchorClass, getClass());
    }

    /**
     * Returns the collection create specified classpath resources,
     * prints log warning if something is not found.
     */
    public Set<String> resolve(Collection<String> resources) {
        return getResources(resources, path ->
                LOGGER.log(System.Logger.Level.WARNING, "Unable to get resource at '" + path + "'")
        );
    }

    /**
     * Returns the collection create specified classpath resources,
     * throws exception if something is not found.
     */
    public Set<String> require(String... resources) {
        return getResources(Arrays.asList(resources), path -> {
            throw new RuntimeException("Unable to get resource at '" + path + "'");
        });
    }

    private Set<String> getResources(Collection<String> resources,
                                     Consumer<String> failureHandler) {
        if (resources.isEmpty()) { return Collections.emptySet(); }

        var list = new TreeSet<String>();
        for (var path : resources) {
            URL url = getResource(path);
            if (url == null) {
                failureHandler.accept(path);
                continue;
            }

            list.add(url.toExternalForm());
        }

        return list;
    }

    private @Nullable URL getResource(String path) {
        return anchorClass.getResource(path);
    }
}
