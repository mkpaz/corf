package corf.base.i18n;

@SuppressWarnings("unused")
public interface M {

    // NOUNS: headers, captions, titles
    String ACTIONS = "Actions";
    String AUTHENTICATION = "Authentication";
    String COMMENT = "Comment";
    String CONFIRMATION = "Confirmation";
    String COUNT = "Count";
    String DATE = "Date";
    String DELIMITER = "Delimiter";
    String DESCRIPTION = "Description";
    String DETAILS = "Details";
    String DOCUMENTATION = "Documentation";
    String DURATION = "Duration";
    String END = "End";
    String ERROR = "Error";
    String EXAMPLE = "Example";
    String FILE = "File";
    String FORMAT = "Format";
    String HEADER = "Header";
    String HELP = "Help";
    String HISTORY = "History";
    String INFO = "Info";
    String IP_ADDRESS = "IPAddress";
    String KEY = "Key";
    String LENGTH = "Length";
    String LOG = "Log";
    String MODE = "Mode";
    String MORE = "More";
    String NAME = "Name";
    String NETMASK = "Netmask";
    String NETWORK = "Network";
    String NO_DATA = "NoData";
    String OPTIONS = "Options";
    String OUTPUT_FILE = "OutputFile";
    String PARAMETER = "Parameter";
    String PARAMETERS = "Parameters";
    String PASSWORD = "Password";
    String PATTERN = "Pattern";
    String PHONE_NUMBER = "PhoneNumber";
    String PORT = "Port";
    String PREFERENCES = "Preferences";
    String PREFIX = "Prefix";
    String RANDOM = "Random";
    String RESULT = "Result";
    String SEARCH = "Search";
    String SEPARATOR = "Separator";
    String SETTINGS = "Settings";
    String START = "Start";
    String STATUS = "Status";
    String STEP = "Step";
    String TEXT = "Text";
    String TIME = "Time";
    String TYPE = "Type";
    String USERNAME = "Username";
    String VALUE = "Value";
    String VERSION = "Version";
    String WARNING = "Warning";

    // VERBS: button or menu actions
    String ACTION_ADD = "action.Add";
    String ACTION_APPLY = "action.Apply";
    String ACTION_BROWSE = "action.Browse";
    String ACTION_CANCEL = "action.Cancel";
    String ACTION_CHOOSE = "action.Choose";
    String ACTION_CLEAR = "action.Clear";
    String ACTION_CLOSE = "action.Close";
    String ACTION_CLOSE_ALL = "action.CloseAll";
    String ACTION_CLOSE_OTHERS = "action.CloseOthers";
    String ACTION_CONFIGURE = "action.Configure";
    String ACTION_COPY = "action.Copy";
    String ACTION_COPY_ALL = "action.CopyAll";
    String ACTION_CUT = "action.Cut";
    String ACTION_DECODE = "action.Decode";
    String ACTION_DELETE = "action.Delete";
    String ACTION_DISABLE = "action.Disable";
    String ACTION_DUPLICATE = "action.Duplicate";
    String ACTION_EDIT = "action.Edit";
    String ACTION_ENABLE = "action.Enable";
    String ACTION_ENCODE = "action.Encode";
    String ACTION_EXPORT = "action.Export";
    String ACTION_GENERATE = "action.Generate";
    String ACTION_IMPORT = "action.Import";
    String ACTION_INSTALL = "action.Install";
    String ACTION_LOAD = "action.Load";
    String ACTION_OK = "action.OK";
    String ACTION_PASTE = "action.Paste";
    String ACTION_PRINT = "action.Print";
    String ACTION_PURGE = "action.Purge";
    String ACTION_QUIT = "action.Quit";
    String ACTION_REDO = "action.Redo";
    String ACTION_REFRESH = "action.Refresh";
    String ACTION_REMOVE = "action.Remove";
    String ACTION_RESTART = "action.Restart";
    String ACTION_RUN = "action.Run";
    String ACTION_SAVE = "action.Save";
    String ACTION_SELECT_ALL = "action.SelectAll";
    String ACTION_START = "action.Start";
    String ACTION_STOP = "action.Stop";
    String ACTION_UNDO = "action.Undo";
    String ACTION_UNINSTALL = "action.Uninstall";
    String ACTION_UPDATE = "action.Update";

    // FILE DIALOG
    String FILE_DIALOG_CSV = "file-dialog.csv";
    String FILE_DIALOG_DAT = "file-dialog.dat";
    String FILE_DIALOG_PDF = "file-dialog.pdf";
    String FILE_DIALOG_TEXT = "file-dialog.text";
    String FILE_DIALOG_XLSX = "file-dialog.xlsx";
    String FILE_DIALOG_XML = "file-dialog.xml";
    String FILE_DIALOG_YAML = "file-dialog.yaml";
    String FILE_DIALOG_ZIP = "file-dialog.zip";

    // MESSAGES
    String MGG_DATABASE_ERROR = "msg.database-error";
    String MSG_GENERIC_ERROR = "msg.generic-error";
    String MSG_GENERIC_IO_ERROR = "msg.generic-io-error";
    String MSG_INVALID_PARAM = "msg.invalid-param";
    String MSG_KEY_IS_NOT_UNIQUE = "msg.key-is-not-unique";
    String MGG_UNABLE_TO_EXTRACT_FILE = "msg.unable-to-extract-file";
    String MGG_UNABLE_TO_LOAD_DATA_FROM_FILE = "msg.unable-to-load-data-from-file";
    String MGG_UNABLE_TO_SAVE_DATA_TO_FILE = "msg.unable-to-save-data-to-file";
    String MGG_CRYPTO_GENERIC_ERROR = "msg.crypto.generic-error";
    String MGG_CRYPTO_UNABLE_TO_ENCRYPT_DATA = "msg.crypto.unable-to-encrypt-data";
    String MGG_CRYPTO_UNABLE_TO_DECRYPT_DATA = "msg.crypto.unable-to-decrypt-data";

    // PLUGIN MANAGER
    String PLUGIN_MSG_ALREADY_INSTALLED = "plugin.msg.already-installed";
    String PLUGIN_MSG_ERROR_WHILE_START = "plugin.msg.error-while-start";
    String PLUGIN_MSG_ERROR_WHILE_STOP = "plugin.msg.error-while-stop";
    String PLUGIN_MSG_HIGHER_PLATFORM_VERSION_REQUIRED = "plugin.msg.higher-platform-version-required";
    String PLUGIN_MSG_PREFIX_INSTALLATION_FAILED = "plugin.msg-prefix.installation-failed";
    String PLUGIN_MSG_INVALID_METADATA = "plugin.msg.invalid-metadata";
    String PLUGIN_MSG_INVALID_NAME = "plugin.msg.invalid-name";
    String PLUGIN_MSG_INVALID_VERSION = "plugin.msg.invalid-version";
    String PLUGIN_MSG_ONLY_ONE_PLUGIN_PER_DIR_ALLOWED = "plugin.msg.only-one-plugin-per-dir-allowed";
    String PLUGIN_MSG_PATH_DOES_NOT_CONTAIN_PLUGINS = "plugin.msg.path-does-not-contain-plugins";
    String PLUGIN_MSG_SOME_PLUGINS_WERE_NOT_STARTED = "plugin.msg.some-plugins-were-not-started";
    String PLUGIN_MSG_SOME_PLUGINS_WERE_NOT_STOPPED = "plugin.msg.some-plugins-were-not-stopped";

    static BundleLoader getLoader() {
        return BundleLoader.of(M.class);
    }
}
