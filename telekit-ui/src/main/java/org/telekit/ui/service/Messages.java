package org.telekit.ui.service;

import org.telekit.base.Settings;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class Messages {

    public static final String PATH = "i18n.messages";

    private Messages() {}

    private static class InstanceHolder {

        private static final Messages INSTANCE = new Messages();
    }

    public static Messages getInstance() {
        return Messages.InstanceHolder.INSTANCE;
    }

    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle(PATH, Settings.LOCALE);
    }

    public static String getMessage(String key) {
        return getInstance().getBundle().getString(key);
    }

    public static String getMessage(String key, Object... args) {
        String pattern = getInstance().getBundle().getString(key);
        return MessageFormat.format(pattern, args);
    }

    public static class Keys {

        public static final String CONFIRMATION = "Confirmation";
        public static final String INFO = "Info";
        public static final String PREFERENCES = "Preferences";
        public static final String STATUS = "Status";
        public static final String QUIT = "Quit";
        public static final String WARNING = "Warning";

        public static final String MSG_GENERIC_IO_ERROR = "msg.generic-io-error";
        public static final String MSG_TASK_COMPLETED = "msg.task-completed";
        public static final String MSG_TASK_CANCELED = "msg.task-canceled";
        public static final String MSG_UNABLE_TO_SAVE_FILE = "msg.unable-to-save-file";
        public static final String MSG_UNABLE_TO_IMPORT_DATA = "msg.unable-to-import-data";
        public static final String MGG_UNABLE_TO_EXPORT_DATA = "msg.unable-to-export-data";
        public static final String MGG_UNABLE_TO_EXTRACT_FILE = "msg.unable-to-extract-file";
        public static final String MGG_UNABLE_TO_PARSE_CONFIG = "msg.unable-to-parse-config";
        public static final String MGG_UNABLE_TO_SAVE_CONFIG = "msg.unable-to-save-config";

        public static final String FILE_DIALOG_TEXT = "file-dialog.text";
        public static final String FILE_DIALOG_XML = "file-dialog.xml";
        public static final String FILE_DIALOG_ZIP = "file-dialog.zip";

        public static final String MAIN_ABOUT = "main.About";
        public static final String MAIN_PLUGIN_MANAGER = "main.PluginManager";
        public static final String MAIN_RESTART_REQUIRED = "main.restart-required";
        public static final String MAIN_MSG_ERROR_OCCURRED = "main.msg.error-occurred";
        public static final String MAIN_TRAY_OPEN = "main.tray.Open";

        public static final String PLUGMAN_MSG_INSTALL_SUCCESS = "plugin-manager.msg.install-success";
        public static final String PLUGMAN_MSG_UNINSTALL_CONFIRM = "plugin-manager.msg.uninstall-confirm";
        public static final String PLUGMAN_MSG_UNINSTALL_SUCCESS = "plugin-manager.msg.uninstall-success";
        public static final String PLUGMAN_MSG_INSTALL_FAILED = "plugin-manager.msg.installation-failed";
        public static final String PLUGMAN_MSG_FILE_IS_NOT_ZIP_ARCHIVE = "plugin-manager.msg.file-is-not-zip-archive";
        public static final String PLUGMAN_MSG_PATH_DOES_NOT_CONTAIN_PLUGINS = "plugin-manager.msg.path-does-not-contain-plugins";
        public static final String PLUGMAN_MSG_ONLY_ONE_PLUGIN_PER_DIR_ALLOWED = "plugin-manager.msg.only-one-plugin-per-dir-allowed";
        public static final String PLUGMAN_MSG_MISSING_PLUGIN_METADATA = "plugin-manager.msg.missing-plugin-metadata";
        public static final String PLUGMAN_MSG_INVALID_PLUGIN_NAME = "plugin-manager.msg.invalid-plugin-name";
        public static final String PLUGMAN_MSG_INVALID_PLUGIN_VERSION = "plugin-manager.msg.invalid-plugin-version";
        public static final String PLUGMAN_MSG_REQUIRE_HIGHER_VERSION = "plugin-manager.msg.require-higher-version";
        public static final String PLUGMAN_MSG_PLUGIN_ALREADY_INSTALLED = "plugin-manager.msg.plugin-already-installed";
        public static final String PLUGMAN_MSG_PLUGIN_SAME_NAME_ALREADY_INSTALLED = "plugin-manager.msg.plugin-same-name-already-installed";

        public static final String TOOLS_APICLIENT = "tools.APIClient";
        public static final String TOOLS_BASE64 = "tools.Base64Encoder";
        public static final String TOOLS_FILEBUILD = "tools.ImportFileBuilder";
        public static final String TOOLS_IPCALC = "tools.IPCalculator";
        public static final String TOOLS_PASSGEN = "tools.PasswordGenerator";
        public static final String TOOLS_SEQGEN = "tools.SequenceGenerator";
        public static final String TOOLS_CICTABLE = "tools.CICTable";
        public static final String TOOLS_SPCCONV = "tools.SPCConverter";
        public static final String TOOLS_TRANSLIT = "tools.Transliterator";

        public static final String TOOLS_ADD_PARAM = "tools.AddParam";
        public static final String TOOLS_NEW_TEMPLATE = "tools.NewTemplate";
        public static final String TOOLS_EDIT_TEMPLATE = "tools.EditTemplate";
        public static final String TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN = "tools.only-first-N-rows-will-be-shown";
        public static final String TOOLS_MSG_DELETE_TEMPLATE = "tools.msg.delete-template";
        public static final String TOOLS_MSG_YOU_HAVE_NO_TEMPLATES_TO_PREVIEW = "tools.msg.you-have-no-templates-to-preview";
        public static final String TOOLS_MSG_VALIDATION_HEAD_0 = "tools.msg.validation.head.0";
        public static final String TOOLS_MSG_VALIDATION_BLANK_LINES = "tools.msg.validation.blank-lines";
        public static final String TOOLS_MSG_VALIDATION_MIXED_CSV = "tools.msg.validation.mixed-csv";
        public static final String TOOLS_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS = "tools.msg.validation.unresolved-placeholders";
        public static final String TOOLS_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED = "tools.msg.validation.csv-threshold-exceeded";
        public static final String TOOLS_MSG_VALIDATION_TAIL_0 = "tools.msg.validation.tail.0";
        public static final String TOOLS_MSG_VALIDATION_TAIL_1 = "tools.msg.validation.tail.1";

        public static final String TOOLS_APICLIENT_TASK_REPORT = "tools.api-client.task-report";
        public static final String TOOLS_IPCALC_TASK_REPORT = "tools.ip-calc.IPFormatConverter";
        public static final String TOOLS_IPCALC_MSG_INVALID_IP_ADDRESS = "tools.ip-calc.msg.invalid-ip-address";
        public static final String TOOLS_SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT = "tools.sequence-generator.msg.sequence-size-exceeds-limit";
        public static final String TOOLS_SS7_MSG_INVALID_POINT_CODE = "tools.ss7.msg.invalid-point-code";
    }
}
