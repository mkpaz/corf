package org.telekit.ui.main;

import org.telekit.base.i18n.BaseMessageKeys;
import org.telekit.controls.i18n.ControlsMessageKeys;

public interface MessageKeys extends BaseMessageKeys, ControlsMessageKeys {

    String MAIN_ABOUT = "main.About";
    String MAIN_PLUGIN_MANAGER = "main.PluginManager";
    String MAIN_RESTART_REQUIRED = "main.restart-required";
    String MAIN_MSG_ERROR_OCCURRED = "main.msg.error-occurred";
    String MAIN_TRAY_OPEN = "main.tray.Open";

    String PLUGIN_MANAGER_MSG_INSTALL_SUCCESS = "plugin-manager.msg.install-success";
    String PLUGIN_MANAGER_MSG_UNINSTALL_CONFIRM = "plugin-manager.msg.uninstall-confirm";
    String PLUGIN_MANAGER_MSG_UNINSTALL_SUCCESS = "plugin-manager.msg.uninstall-success";

    String TOOLS_APICLIENT = "tools.APIClient";
    String TOOLS_BASE64 = "tools.Base64Encoder";
    String TOOLS_FILEBUILD = "tools.ImportFileBuilder";
    String TOOLS_IPCALC = "tools.IPCalculator";
    String TOOLS_PASSGEN = "tools.PasswordGenerator";
    String TOOLS_SEQGEN = "tools.SequenceGenerator";
    String TOOLS_CICTABLE = "tools.CICTable";
    String TOOLS_SPCCONV = "tools.SPCConverter";
    String TOOLS_TRANSLIT = "tools.Transliterator";

    String TOOLS_ADD_PARAM = "tools.AddParam";
    String TOOLS_NEW_TEMPLATE = "tools.NewTemplate";
    String TOOLS_EDIT_TEMPLATE = "tools.EditTemplate";
    String TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN = "tools.only-first-N-rows-will-be-shown";
    String TOOLS_MSG_DELETE_TEMPLATE = "tools.msg.delete-template";
    String TOOLS_MSG_YOU_HAVE_NO_TEMPLATES_TO_PREVIEW = "tools.msg.you-have-no-templates-to-preview";
    String TOOLS_MSG_VALIDATION_HEAD_0 = "tools.msg.validation.head.0";
    String TOOLS_MSG_VALIDATION_BLANK_LINES = "tools.msg.validation.blank-lines";
    String TOOLS_MSG_VALIDATION_MIXED_CSV = "tools.msg.validation.mixed-csv";
    String TOOLS_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS = "tools.msg.validation.unresolved-placeholders";
    String TOOLS_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED = "tools.msg.validation.csv-threshold-exceeded";
    String TOOLS_MSG_VALIDATION_TAIL_0 = "tools.msg.validation.tail.0";
    String TOOLS_MSG_VALIDATION_TAIL_1 = "tools.msg.validation.tail.1";

    String TOOLS_APICLIENT_TASK_REPORT = "tools.api-client.task-report";
    String TOOLS_IPCALC_TASK_REPORT = "tools.ip-calc.IPFormatConverter";
    String TOOLS_IPCALC_MSG_INVALID_IP_ADDRESS = "tools.ip-calc.msg.invalid-ip-address";
    String TOOLS_SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT = "tools.sequence-generator.msg.sequence-size-exceeds-limit";
    String TOOLS_SS7_MSG_INVALID_POINT_CODE = "tools.ss7.msg.invalid-point-code";
}
