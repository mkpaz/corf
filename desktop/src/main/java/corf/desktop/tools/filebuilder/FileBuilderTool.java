package corf.desktop.tools.filebuilder;

import backbonefx.event.EventSource;
import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import corf.base.plugin.Tool;
import corf.base.plugin.ToolGroup;
import corf.desktop.i18n.DM;

import java.util.Objects;

import static corf.base.i18n.I18n.t;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class FileBuilderTool implements Tool<FileBuilderView> {

    public static final EventSource EVENT_SOURCE = new EventSource(FileBuilderTool.class.getCanonicalName());

    public static final Image ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/tools/filebuilder_64.png").getResourceAsStream()
    ));

    @Override
    public String getName() {
        return t(DM.FILE_BUILDER);
    }

    @Override
    public @Nullable ToolGroup getGroup() {
        return null;
    }

    @Override
    public Class<FileBuilderView> getView() {
        return FileBuilderView.class;
    }

    @Override
    public Image getIcon() {
        return ICON;
    }
}
