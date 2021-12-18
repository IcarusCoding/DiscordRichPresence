package de.intelligence.drp.exception;

public final class ConnectionFailureException extends RuntimeException {

    public ConnectionFailureException() {
        super();
    }

    public ConnectionFailureException(String message) {
        super(message);
    }

    public ConnectionFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
