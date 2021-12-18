package de.intelligence.drp.exception;

public final class ReadFailureException extends RuntimeException {

    public ReadFailureException() {
        super();
    }

    public ReadFailureException(String message) {
        super(message);
    }

    public ReadFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
