package telekit.controls.theme;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.Nullable;
import telekit.base.preferences.Theme;
import telekit.base.util.ClasspathResource;

import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

public class DefaultTheme implements Theme {

    private static final ClasspathResource MODULE_PATH = ClasspathResource.of("/telekit/controls");
    private static final ClasspathResource FONTS_DIR = MODULE_PATH.concat("assets/fonts");
    private static final ClasspathResource STYLES_DIR = MODULE_PATH.concat("assets/theme");
    private static final String FORMAT_TTF = "ttf";
    private static final String FORMAT_OTF = "otf";

    static final Set<String> STYLESHEETS = Set.of(
            STYLES_DIR.concat("index.css").toString(),    // color variables and defaults
            STYLES_DIR.concat("tweaks.css").toString(),   // modena tweaks
            STYLES_DIR.concat("controls.css").toString(), // custom controls and utils
            STYLES_DIR.concat("widgets.css").toString()
    );

    public DefaultTheme() { }

    @Override
    public String getName() { return "default"; }

    @Override
    public Set<String> getStylesheets() {
        return STYLESHEETS;
    }

    @Override
    public boolean isLight() { return true; }

    @Override
    public InputStream getInterfaceFont(FontWeight weight, FontPosture posture) {
        return DefaultTheme.class.getResourceAsStream(Objects.requireNonNull(
                getFont("InterUI", FORMAT_OTF, weight, posture)
        ));
    }

    @Override
    public InputStream getMonospaceFont(FontWeight weight) {
        return DefaultTheme.class.getResourceAsStream(Objects.requireNonNull(
                getFont("FiraMono", FORMAT_TTF, weight, FontPosture.REGULAR)
        ));
    }

    @Override
    public InputStream getDocumentFont(FontWeight weight, FontPosture posture) {
        return DefaultTheme.class.getResourceAsStream(Objects.requireNonNull(
                getFont("Roboto", FORMAT_TTF, weight, posture)
        ));
    }

    @Nullable String getFont(String fontFamily,
                                     String fontFormat,
                                     FontWeight weight,
                                     FontPosture posture) {
        return switch (weight) {
            case BOLD, EXTRA_BOLD, BLACK -> getFontPath(fontFamily, "Bold", fontFormat);
            case MEDIUM, SEMI_BOLD -> getFontPath(fontFamily, "Medium", fontFormat);
            default -> posture != FontPosture.ITALIC ?
                    getFontPath(fontFamily, "Regular", fontFormat) :
                    getFontPath(fontFamily, "Italic", fontFormat);
        };
    }

    static String getFontPath(String fontFamily, String fontType, String fontFormat) {
        String subPath = fontFamily + "/" + fontFamily + "-" + fontType + "." + fontFormat;
        return FONTS_DIR.concat(subPath).toString();
    }
}
