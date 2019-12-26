package org.telekit.controls.theme;

import org.telekit.base.util.ClasspathResource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DefaultTheme implements Theme {

    private static final List<String> PATHS = Arrays.asList(
            "assets/theme/index.css",    // color variables and defaults
            "assets/theme/tweaks.css",   // modena tweaks
            "assets/theme/controls.css", // custom controls and utils
            "assets/theme/widgets.css"
    );

    public DefaultTheme() {}

    @Override
    public String getName() { return "default"; }

    @Override
    public Set<String> getResources() {
        return getResources(ClasspathResource.of("/org/telekit/controls", null), PATHS);
    }

    @Override
    public boolean isLight() { return true; }
}
