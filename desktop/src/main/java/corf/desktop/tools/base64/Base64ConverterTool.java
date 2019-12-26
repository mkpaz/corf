package corf.desktop.tools.base64;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import corf.base.plugin.Tool;
import corf.base.plugin.ToolGroup;
import corf.desktop.i18n.DM;

import java.util.Objects;

import static corf.base.i18n.I18n.t;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class Base64ConverterTool implements Tool<Base64ConverterView> {

    public static final Image ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/tools/base64_64.png").getResourceAsStream()
    ));

    @Override
    public String getName() {
        return t(DM.BASE64_CONVERTER);
    }

    @Override
    public @Nullable ToolGroup getGroup() {
        return null;
    }

    @Override
    public Class<Base64ConverterView> getView() {
        return Base64ConverterView.class;
    }

    @Override
    public Image getIcon() {
        return ICON;
    }
}
