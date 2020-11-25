package org.telekit.base.domain.exception;

import org.telekit.base.i18n.Messages;

import static org.telekit.base.i18n.BaseMessageKeys.MGG_CRYPTO_KEY_VAULT_IS_LOCKED;

public class VaultLockedException extends TelekitException {

    public VaultLockedException() {
        super(Messages.get(MGG_CRYPTO_KEY_VAULT_IS_LOCKED));
    }

    public VaultLockedException(String message) {
        super(message);
    }

    public VaultLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
