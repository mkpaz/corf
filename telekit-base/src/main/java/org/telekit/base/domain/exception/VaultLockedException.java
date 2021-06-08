package org.telekit.base.domain.exception;

import org.telekit.base.i18n.I18n;

import static org.telekit.base.i18n.BaseMessages.MGG_CRYPTO_KEY_VAULT_IS_LOCKED;

public class VaultLockedException extends TelekitException {

    public VaultLockedException() {
        super(I18n.t(MGG_CRYPTO_KEY_VAULT_IS_LOCKED));
    }

    public VaultLockedException(String message) {
        super(message);
    }

    public VaultLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
