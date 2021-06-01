package org.telekit.desktop.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Tool;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.UILoader;
import org.telekit.desktop.MessageKeys;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public enum BuiltinTool implements Tool {

    //@formatter:off
    API_CLIENT    (MessageKeys.TOOLS_API_CLIENT, null, false),
    BASE64        (MessageKeys.TOOLS_BASE64, null, false),
    FILE_BUILDER  (MessageKeys.TOOLS_FILE_BUILDER, null, false),
    IP4_CALC      (MessageKeys.TOOLS_IP4_CALC, null, false),
    PASS_GEN      (MessageKeys.TOOLS_PASS_GEN, null, false),
    SEQ_GEN       (MessageKeys.TOOLS_SEQ_GEN, null, false),
    SS7_SPC_CONV  (MessageKeys.TOOLS_SS7_SPC_CONV, MessageKeys.TOOLS_GROUP_SS7, true),
    SS7_CIC_TABLE (MessageKeys.TOOLS_SS7_CIC_TABLE, MessageKeys.TOOLS_GROUP_SS7, false),
    TRANSLIT      (MessageKeys.TOOLS_TRANSLIT, null, false);
    //@formatter:on

    private final String nameKey;
    private final String groupNameKey;
    private final boolean modal;

    BuiltinTool(String nameKey, String groupNameKey, boolean modal) {
        this.nameKey = Objects.requireNonNull(nameKey);
        this.groupNameKey = groupNameKey;
        this.modal = modal;
    }

    @Override
    public @NotNull String getName() {
        return Messages.get(nameKey);
    }

    @Override
    public @Nullable String getGroupName() {
        return isNotBlank(groupNameKey) ? Messages.get(groupNameKey) : null;
    }

    @Override
    public boolean isModal() {
        return modal;
    }

    @Override
    public @NotNull Controller createController() {
        Messages resourceBundle = Messages.getInstance();
        switch (this) {
            case API_CLIENT -> {
                return UILoader.load(FXMLView.API_CLIENT.getLocation(), resourceBundle);
            }
            case BASE64 -> {
                return UILoader.load(FXMLView.BASE64.getLocation(), resourceBundle);
            }
            case FILE_BUILDER -> {
                return UILoader.load(FXMLView.FILE_BUILDER.getLocation(), resourceBundle);
            }
            case IP4_CALC -> {
                return UILoader.load(FXMLView.IPV4_CALC.getLocation(), resourceBundle);
            }
            case PASS_GEN -> {
                return UILoader.load(FXMLView.PASS_GEN.getLocation(), resourceBundle);
            }
            case SEQ_GEN -> {
                return UILoader.load(FXMLView.SEQ_GEN.getLocation(), resourceBundle);
            }
            case SS7_CIC_TABLE -> {
                return UILoader.load(FXMLView.SS7_CIC_TABLE.getLocation(), resourceBundle);
            }
            case SS7_SPC_CONV -> {
                return UILoader.load(FXMLView.SS7_SPC_CONV.getLocation(), resourceBundle);
            }
            case TRANSLIT -> {
                return UILoader.load(FXMLView.TRANSLIT.getLocation(), resourceBundle);
            }
        }
        throw new RuntimeException("Unable to create controller: unknown tool ID.");
    }
}