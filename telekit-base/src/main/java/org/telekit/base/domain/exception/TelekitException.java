package org.telekit.base.domain.exception;

public class TelekitException extends RuntimeException {

    public TelekitException(String message) {
        super(message);
    }

    public TelekitException(String message, Throwable cause) {
        super(message, cause);
    }
}
