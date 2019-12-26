package corf.desktop.tools.ipcalc;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import corf.base.plugin.Tool;
import corf.base.plugin.ToolGroup;
import corf.desktop.i18n.DM;

import java.util.Objects;

import static corf.base.i18n.I18n.t;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class IPv4CalcTool implements Tool<IPv4CalcView> {

    public static final Image ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/tools/ipcalc_64.png").getResourceAsStream()
    ));

    @Override
    public String getName() {
        return t(DM.IPV4_CALCULATOR);
    }

    @Override
    public @Nullable ToolGroup getGroup() {
        return null;
    }

    @Override
    public Class<IPv4CalcView> getView() {
        return IPv4CalcView.class;
    }

    @Override
    public Image getIcon() {
        return ICON;
    }
}
