package corf.base.exception;

import static corf.base.i18n.M.MGG_DATABASE_ERROR;
import static corf.base.i18n.I18n.t;

public class DatabaseException extends AppException {

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
