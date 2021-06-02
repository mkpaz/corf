package org.telekit.controls.theme;

import org.telekit.controls.demo.DemoLauncher;

import java.net.URL;
import java.util.Objects;
import java.util.Set;

public class ThemeLoader {

    public static final String THEMES_DIR_PATH = "/org/telekit/controls/assets/themes/";

    public Set<String> getStylesheets(String theme) {
        // multiple themes is not yet supported
        return Set.of(
                getResource(ThemeLoader.THEMES_DIR_PATH + "index.css").toExternalForm(),
                getResource(ThemeLoader.THEMES_DIR_PATH + "base.css").toExternalForm()
        );
    }

    public static URL getResource(String resource) {
        return Objects.requireNonNull(DemoLauncher.class.getResource(resource));
    }
}
