package org.telekit.base.i18n;

// Should contain any error or informational messages, which can be used
// both in services and desktop dialogs.
public interface BaseMessages {

    // COMMON MESSAGES
    String MGG_DATABASE_ERROR = "msg.database-error";
    String MSG_GENERIC_ERROR = "msg.generic-error";
    String MSG_GENERIC_IO_ERROR = "msg.generic-io-error";
    String MSG_INVALID_PARAM = "msg.invalid-param";
    String MSG_KEY_IS_NOT_UNIQUE = "msg.key-is-not-unique";
    String MGG_UNABLE_TO_EXTRACT_FILE = "msg.unable-to-extract-file";
    String MGG_UNABLE_TO_LOAD_DATA_FROM_FILE = "msg.unable-to-load-data-from-file";
    String MGG_UNABLE_TO_SAVE_DATA_TO_FILE = "msg.unable-to-save-data-to-file";
    String MGG_CRYPTO_GENERIC_ERROR = "msg.crypto.generic-error";
    String MGG_CRYPTO_KEY_IS_NOT_PRESENT = "msg.crypto.key-is-not-present";
    String MGG_CRYPTO_KEY_VAULT_IS_LOCKED = "msg.crypto.key-vault-is-locked";
    String MGG_CRYPTO_UNABLE_TO_ENCRYPT_DATA = "msg.crypto.unable-to-encrypt-data";
    String MGG_CRYPTO_UNABLE_TO_DECRYPT_DATA = "msg.crypto.unable-to-decrypt-data";

    // PLUGINS

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

    static BundleLoader getLoader() { return BundleLoader.of(BaseMessages.class); }
}
