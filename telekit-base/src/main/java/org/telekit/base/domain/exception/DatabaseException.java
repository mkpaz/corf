package org.telekit.base.domain.exception;

import static org.telekit.base.i18n.BaseMessages.MGG_DATABASE_ERROR;
import static org.telekit.base.i18n.I18n.t;

public class DatabaseException extends TelekitException {

    public DatabaseException() {
        super(t(MGG_DATABASE_ERROR));
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
