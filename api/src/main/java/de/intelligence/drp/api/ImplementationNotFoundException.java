package de.intelligence.drp.api;

public final class ImplementationNotFoundException extends RuntimeException {

    public ImplementationNotFoundException(String message) {
        super(message);
    }

    public ImplementationNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
