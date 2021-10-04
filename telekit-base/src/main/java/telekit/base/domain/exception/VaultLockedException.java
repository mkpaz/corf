package telekit.base.domain.exception;

import static telekit.base.i18n.BaseMessages.MGG_CRYPTO_KEY_VAULT_IS_LOCKED;
import static telekit.base.i18n.I18n.t;

public class VaultLockedException extends TelekitException {

    public VaultLockedException() {
        super(t(MGG_CRYPTO_KEY_VAULT_IS_LOCKED));
    }

    public VaultLockedException(String message) {
        super(message);
    }

    public VaultLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
