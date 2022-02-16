package de.intelligence.drp.core.exception;

public final class InitializationFailedException extends RuntimeException {

    public InitializationFailedException() {
        super();
    }

    public InitializationFailedException(String message) {
        super(message);
    }

    public InitializationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
