package org.telekit.desktop.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Tool;
import org.telekit.desktop.MessageKeys;
import org.telekit.desktop.tools.apiclient.ApiClientController;
import org.telekit.desktop.tools.base64.Base64Controller;
import org.telekit.desktop.tools.filebuilder.FileBuilderController;
import org.telekit.desktop.tools.ipcalc.IPv4Controller;
import org.telekit.desktop.tools.passgen.PasswordGeneratorController;
import org.telekit.desktop.tools.seqgen.SequenceGeneratorController;
import org.telekit.desktop.tools.ss7.CICTableController;
import org.telekit.desktop.tools.ss7.SPCConverterController;
import org.telekit.desktop.tools.translit.TranslitController;

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
    public @NotNull Component createComponent() {
        //@formatter:off
        switch (this) {
            case API_CLIENT ->    { return ViewLoader.load(ApiClientController.class);           }
            case BASE64 ->        { return ViewLoader.load(Base64Controller.class);              }
            case FILE_BUILDER ->  { return ViewLoader.load(FileBuilderController.class);         }
            case IP4_CALC ->      { return ViewLoader.load(IPv4Controller.class);                }
            case PASS_GEN ->      { return ViewLoader.load(PasswordGeneratorController.class);   }
            case SEQ_GEN ->       { return ViewLoader.load(SequenceGeneratorController.class);            }
            case SS7_CIC_TABLE -> { return ViewLoader.load(CICTableController.class);            }
            case SS7_SPC_CONV ->  { return ViewLoader.load(SPCConverterController.class);        }
            case TRANSLIT ->      { return ViewLoader.load(TranslitController.class);            }
        }
        //@formatter:om
        throw new RuntimeException("Unable to create controller: unknown tool ID.");
    }
}