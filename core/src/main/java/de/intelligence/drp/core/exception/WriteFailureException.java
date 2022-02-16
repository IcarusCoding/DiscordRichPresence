package de.intelligence.drp.core.exception;

public final class WriteFailureException extends RuntimeException {

    public WriteFailureException() {
        super();
    }

    public WriteFailureException(String message) {
        super(message);
    }

    public WriteFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}