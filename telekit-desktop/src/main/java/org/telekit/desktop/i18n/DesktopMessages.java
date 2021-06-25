package org.telekit.desktop.i18n;

import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.controls.i18n.ControlsMessages;

// TODO: Add all entries from messages file (for now ignore test warnings)
public interface DesktopMessages extends BaseMessages, ControlsMessages {

    String SYSTEM_HOME = "system.Home";
    String SYSTEM_PROJECT_PAGE = "system.ProjectPage";
    String SYSTEM_OPEN_DATA_DIR = "system.OpenDataDirectory";
    String SYSTEM_OPEN_PLUGINS_DIR = "system.OpenPluginsDirectory";
    String SYSTEM_MSG_ERROR_OCCURRED = "system.msg.error-occurred";
    String SYSTEM_RESTART_REQUIRED = "system.RestartRequired";

    String PREFERENCES_GENERAL = "preferences.General";
    String PREFERENCES_PROXY = "preferences.Proxy";
    String PREFERENCES_REQUIRES_RESTART = "preferences.requires-restart";

    String PLUGIN_MANAGER_MSG_INSTALL_SUCCESS = "plugin-manager.msg.install-success";
    String PLUGIN_MANAGER_MSG_UNINSTALL_CONFIRM = "plugin-manager.msg.uninstall-confirm";
    String PLUGIN_MANAGER_MSG_UNINSTALL_SUCCESS = "plugin-manager.msg.uninstall-success";

    String TOOLS_API_CLIENT = "tools.APIClient";
    String TOOLS_BASE64 = "tools.Base64Encoder";
    String TOOLS_FILE_BUILDER = "tools.ImportFileBuilder";
    String TOOLS_IP4_CALC = "tools.IPCalculator";
    String TOOLS_PASS_GEN = "tools.PasswordGenerator";
    String TOOLS_SEQ_GEN = "tools.SequenceGenerator";

    String TOOLS_CHOOSE_VALUE = "tools.ChooseValue";
    String TOOLS_BATCH = "tools.Batch";
    String TOOLS_COMMA = "tools.comma";
    String TOOLS_COLON = "tools.colon";
    String TOOLS_LIST_FOR_REPLACEMENT = "tools.ListForReplacement";
    String TOOLS_LINES = "tools.lines";
    String TOOLS_PIPE = "tools.pipe";
    String TOOLS_SEMICOLON = "tools.semicolon";
    String TOOLS_SECONDS = "tools.seconds";
    String TOOLS_TAB = "tools.tab";
    String TOOLS_TEMPLATE = "tools.Template";
    String TOOLS_SHOW_SAVE_DIALOG = "tools.ShowSaveDialog";
    String TOOLS_SAVE_AS = "tools.SaveAs";
    String TOOLS_APPEND_IF_EXISTS = "tools.append-if-exists";
    String TOOLS_DOCUMENT_START = "tools.DocumentStart";
    String TOOLS_DOCUMENT_END = "tools.DocumentEnd";
    String TOOLS_ELEMENT = "tools.Element";

    String TOOLS_ADD_PARAM = "tools.AddParam";
    String TOOLS_NEW_TEMPLATE = "tools.NewTemplate";
    String TOOLS_EDIT_TEMPLATE = "tools.EditTemplate";
    String TOOLS_PASTE_COLUMNS_RIGHT = "tools.PasteColumnsRight";
    String TOOLS_PASTE_FROM_EXCEL = "tools.PasteFromExcel";
    String TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN = "tools.only-first-N-rows-will-be-shown";
    String TOOLS_MSG_DELETE_TEMPLATE = "tools.msg.delete-template";
    String TOOLS_MSG_YOU_HAVE_NO_TEMPLATES_TO_PREVIEW = "tools.msg.you-have-no-templates-to-preview";
    String TOOLS_MSG_VALIDATION_HEAD = "tools.msg.validation.head";
    String TOOLS_MSG_VALIDATION_BLANK_PARAM_VALUES = "tools.msg.validation.blank-param-values";
    String TOOLS_MSG_VALIDATION_MIXED_CSV = "tools.msg.validation.mixed-csv";
    String TOOLS_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS = "tools.msg.validation.unresolved-placeholders";
    String TOOLS_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED = "tools.msg.validation.csv-threshold-exceeded";
    String TOOLS_MSG_VALIDATION_TAIL = "tools.msg.validation.tail";

    String TOOLS_APICLIENT_BATCH_SIZE = "tools.api-client.BatchSize";
    String TOOLS_APICLIENT_BATCH_WRAPPER = "tools.api-client.BatchWrapper";
    String TOOLS_APICLIENT_COMPLETED_REQUESTS = "tools.api-client.CompletedRequests";
    String TOOLS_APICLIENT_BODY = "tools.api-client.Body";
    String TOOLS_APICLIENT_WAIT_TIMEOUT = "tools.api-client.WaitTimeout";
    String TOOLS_APICLIENT_HTTP_HEADERS = "tools.api-client.HTTPHeaders";
    String TOOLS_APICLIENT_TASK_REPORT = "tools.api-client.task-report";
    String TOOLS_APICLIENT_REQUEST_LINE = "tools.api-client.RequestLine";
    String TOOLS_APICLIENT_SHOW_UNSUCCESSFUL_REQUESTS_ONLY = "tools.api-client.show-unsuccessful-requests-only";
    String TOOLS_APICLIENT_SHOW_TIMEOUT_BETWEEN_REQUESTS = "tools.api-client.timeout-between-requests";
    String TOOLS_IPCALC_TASK_REPORT = "tools.ipcalc.IPFormatConverter";
    String TOOLS_IPCALC_MSG_INVALID_IP_ADDRESS = "tools.ipcalc.msg.invalid-ip-address";
    String TOOLS_SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT = "tools.seq-gen.msg.sequence-size-exceeds-limit";
    String TOOLS_FILE_BUILDER_OPEN_FILE_AFTER_GENERATION = "tools.file-builder.open-file-after-generation";
    String TOOLS_BASE64_AS_TEXT = "tools.base64.as-text";
    String TOOLS_BASE64_LINE_BY_LINE = "tools.base64.line-by-line";

    static BundleLoader getLoader() { return BundleLoader.of(DesktopMessages.class); }
}
