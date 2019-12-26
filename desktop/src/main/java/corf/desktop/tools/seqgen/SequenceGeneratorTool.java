package corf.desktop.tools.seqgen;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import corf.base.plugin.Tool;
import corf.base.plugin.ToolGroup;
import corf.desktop.i18n.DM;

import java.util.Objects;

import static corf.base.i18n.I18n.t;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class SequenceGeneratorTool implements Tool<SequenceGeneratorView> {

    public static final Image ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/tools/seqgen_64.png").getResourceAsStream()
    ));

    @Override
    public String getName() {
        return t(DM.SEQUENCE_GENERATOR);
    }

    @Override
    public @Nullable ToolGroup getGroup() {
        return null;
    }

    @Override
    public Class<SequenceGeneratorView> getView() {
        return SequenceGeneratorView.class;
    }

    @Override
    public Image getIcon() {
        return ICON;
    }
}
