package de.intelligence.drp.exception;

public final class AlreadyInitializedException extends RuntimeException {

    public AlreadyInitializedException() {
        super();
    }

    public AlreadyInitializedException(String message) {
        super(message);
    }

    public AlreadyInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

}
