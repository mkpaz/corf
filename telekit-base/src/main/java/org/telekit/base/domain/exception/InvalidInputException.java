package org.telekit.base.domain.exception;

/**
 * Checked exception that can be thrown if one (or few) input args
 * invalid or malformed.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
