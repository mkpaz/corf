package org.telekit.controls.theme;

import org.telekit.base.util.ClasspathResource;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface Theme {

    String getName();

    Set<String> getResources();

    boolean isLight();

    default Set<String> getResources(ClasspathResource resource, Collection<String> subPaths) {
        return subPaths.stream()
                .distinct()
                .map(p -> resource.concat(p).toString())
                .map(p -> Objects.requireNonNull(getClass().getResource(p)).toExternalForm())
                .collect(Collectors.toSet());
    }
}
