package org.telekit.controls.theme;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.telekit.base.preferences.Theme;
import org.telekit.base.util.ClasspathResource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DefaultTheme implements Theme {

    private static final ClasspathResource MODULE_PATH = ClasspathResource.of("/org/telekit/controls");
    private static final ClasspathResource FONTS_DIR = MODULE_PATH.concat("assets/fonts");

    private static final List<String> STYLES = Arrays.asList(
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
        return getResources(MODULE_PATH, STYLES);
    }

    @Override
    public boolean isLight() { return true; }

    @Override
    public InputStream getRegularFont(FontWeight weight, FontPosture posture) {
        String fontFamily = "Roboto";

        return switch (weight) {
            case BOLD, EXTRA_BOLD, BLACK -> getFont(fontFamily, "Bold.ttf");
            case MEDIUM, SEMI_BOLD -> getFont(fontFamily, "Medium.ttf");
            default -> posture != FontPosture.ITALIC ?
                    getFont(fontFamily, "Regular.ttf") :
                    getFont(fontFamily, "Italic.ttf");
        };
    }

    @Override
    public InputStream getMonospaceFont(FontWeight weight) {
        String fontFamily = "FiraMono";

        return switch (weight) {
            case BOLD, EXTRA_BOLD, BLACK -> getFont(fontFamily, "Bold.ttf");
            case MEDIUM, SEMI_BOLD -> getFont(fontFamily, "Medium.ttf");
            default -> getFont(fontFamily, "Regular.ttf");
        };
    }

    static InputStream getFont(String fontFamily, String fontType) {
        ClasspathResource resource = FONTS_DIR.concat(fontFamily + "/" + fontFamily + "-" + fontType);
        return DefaultTheme.class.getResourceAsStream(resource.toString());
    }
}
