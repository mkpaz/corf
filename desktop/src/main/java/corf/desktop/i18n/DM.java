package corf.desktop.i18n;

import corf.base.i18n.BundleLoader;
import corf.base.i18n.M;

public interface DM extends M {

    String BASE64 = "Base64";
    String COLOR_THEME = "ColorTheme";
    String PLUGINS = "Plugins";
    String PROJECT_PAGE = "ProjectPage";
    String PROXY = "Proxy";
    String REQUIRED_FIELD = "RequiredField";
    String TOOLS = "Tools";

    String COPY_TO_CLIPBOARD = "CopyToClipboard";
    String SAVE_TO_FILE = "SaveToFile";

    String PREFS_CHECK_CONNECTION = "prefs.CheckConnection";
    String PREFS_PROXY_EXCEPTIONS = "prefs.Exceptions";
    String PREFS_MSG_PROXY_CONNECTION_SUCCESSFUL = "prefs.msg.proxy-connection-successful";
    String PREFS_MSG_PROXY_CONNECTION_FAILED = "prefs.msg.proxy-connection-failed";

    String PLUGIN_MSG_INSTALL_SUCCESS = "plugin.msg.install-success";
    String PLUGIN_MSG_UNINSTALL_SUCCESS = "plugin.msg.uninstall-success";

    String TPL_ADD_NEW_TEMPLATE = "tpl.AddNewTemplate";
    String TPL_CHOOSE_VALUE = "tpl.ChooseValue";
    String TPL_EDIT_TEMPLATE = "tpl.EditTemplate";
    String TPL_LINES = "tpl.lines";
    String TPL_MANAGE_TEMPLATES = "tpl.ManageTemplates";
    String TPL_MSG_LIST_IS_TOO_LARGE_TO_EXPORT = "tpl.msg.list-is-too-large-to-export";
    String TPL_MSG_VALIDATION_BLANK_PARAM_VALUES = "tpl.msg.validation.blank-param-values";
    String TPL_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED = "tpl.msg.validation.csv-threshold-exceeded";
    String TPL_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS = "tpl.msg.validation.unresolved-placeholders";
    String TPL_MSG_VALIDATION_VARIABLE_CSV_LENGTH = "tpl.msg.validation.variable-csv-length";
    String TPL_NAMED_PARAMS = "tpl.NamedParams";
    String TPL_NO_NAMED_PARAMS_IN_SELECTED_TEMPLATE = "tpl.NoNamedParametersInSelectedTemplate";
    String TPL_OR_SELECT_EXISTING_TEMPLATE_TO_EDIT = "tpl.OrSelectExistingTemplateToEdit";
    String TPL_PASTE_COLUMNS_RIGHT = "tpl.PasteColumnsRight";
    String TPL_PASTE_FROM_EXCEL = "tpl.PasteFromExcel";
    String TPL_ROW_PARAMS = "tpl.RowParams";
    String TPL_SELECT_VALUE_FROM_THE_LIST = "tpl.SelectValueFromTheList";
    String TPL_TEMPLATE = "tpl.Template";
    String TPL_TEMPLATE_HAS_NO_DESCRIPTION = "tpl.TemplateHasNoDescription";
    String TPL_TEMPLATE_MANAGER = "tpl.TemplateManager";

    String HTTP_SENDER = "HttpSender";
    String HTTP_SENDER_BATCH = "httpSender.Batch";
    String HTTP_SENDER_BATCH_SIZE = "httpSender.BatchSize";
    String HTTP_SENDER_BODY = "httpSender.Body";
    String HTTP_SENDER_COMPLETED_REQUESTS = "httpSender.CompletedRequests";
    String HTTP_SENDER_ENABLE_BASIC_AUTHENTICATION = "httpSender.EnableBasicAuthentication";
    String HTTP_SENDER_HEADERS = "httpSender.Headers";
    String HTTP_SENDER_MSG_BATCH_INFO = "httpSender.msg.batch-info";
    String HTTP_SENDER_SHOW_UNSUCCESSFUL_REQUESTS_ONLY = "httpSender.ShowUnsuccessfulRequestsOnly";
    String HTTP_SENDER_TIMEOUT_BETWEEN_REQUESTS = "httpSender.TimeoutBetweenRequests";
    String HTTP_SENDER_WAIT_TIMEOUT = "httpSender.WaitTimeout";

    String BASE64_CONVERTER = "Base64Converter";
    String BASE64_ALGORITHM = "base64.Algorithm";
    String BASE64_LINE_BY_LINE = "base64.ConvertLineByLine";

    String FILE_BUILDER = "FileBuilder";
    String FILE_BUILDER_AFTER_THE_LAST_ELEMENT = "fileBuilder.AfterTheLastElement";
    String FILE_BUILDER_APPEND_TO_THE_PREVIOUS_FILE = "fileBuilder.AppendToThePreviousFile";
    String FILE_BUILDER_BEFORE_THE_FIRST_ELEMENT = "fileBuilder.BeforeTheFirstElement";
    String FILE_BUILDER_BETWEEN_ELEMENTS = "fileBuilder.BetweenElements";
    String FILE_BUILDER_CREATE_NEW_FILE = "fileBuilder.CreateNewFile";
    String FILE_BUILDER_DOCUMENT_END = "fileBuilder.DocumentEnd";
    String FILE_BUILDER_DOCUMENT_START = "fileBuilder.DocumentStart";
    String FILE_BUILDER_ELEMENT = "fileBuilder.Element";

    String IPV4_CALCULATOR = "IPv4Calculator";
    String IPV4CALC_GENERATE_IP_ADDRESSES = "ipv4calc.GenerateIPAddressList";
    String IPV4CALC_IP_ADDRESS_CONVERTER = "ipv4calc.IPAddressConverter";
    String IPV4CALC_NETMASKS = "ipv4calc.Netmasks";
    String IPV4CALC_NETWORK_INFO = "ipv4calc.NetworkInfo";
    String IPV4CALC_SPLIT_NETWORK = "ipv4calc.SplitNetwork";
    String IPV4CALC_SPLIT_TO_0 = "ipv4calc.split-to.0";
    String IPV4CALC_SPLIT_TO_1 = "ipv4calc.split-to.1";

    String PASSWORD_GENERATOR = "PasswordGenerator";
    String PASSGEN_CHARACTER_SET = "passgen.CharacterSet";
    String PASSGEN_CHARACTERS = "passgen.Characters";
    String PASSGEN_ENTROPY = "passgen.Entropy";
    String PASSGEN_EXCELLENT = "passgen.Excellent";
    String PASSGEN_EXCLUDE = "passgen.Exclude";
    String PASSGEN_EXCLUDE_SIMILAR_CHARS = "passgen.ExcludeSimilarChars";
    String PASSGEN_GOOD = "passgen.Good";
    String PASSGEN_INCLUDE = "passgen.Include";
    String PASSGEN_KATAKANA = "passgen.Katakana";
    String PASSGEN_LETTER_CASE = "passgen.LetterCase";
    String PASSGEN_MODERATE = "passgen.Moderate";
    String PASSGEN_PASSWORD = "passgen.GeneratedPassword";
    String PASSGEN_PASSWORDS = "passgen.Passwords";
    String PASSGEN_POOR = "passgen.Poor";
    String PASSGEN_WEAK = "passgen.Weak";
    String PASSGEN_WORDS = "passgen.Words";

    String SEQUENCE_GENERATOR = "SequenceGenerator";
    String SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT = "seqgen.msg.sequence-size-exceeds-limit";

    static BundleLoader getLoader() {
        return BundleLoader.of(DM.class);
    }
}
