package org.telekit.base.domain;

public class TelekitException extends RuntimeException {

    public TelekitException(String message) {
        super(message);
    }

    public TelekitException(String message, Throwable cause) {
        super(message, cause);
    }

    public static void fire(String message) {
        throw new TelekitException(message);
    }
}
