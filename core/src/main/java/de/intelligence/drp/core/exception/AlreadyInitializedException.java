package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class AlreadyInitializedException extends ConnectionException {

    public AlreadyInitializedException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable, errorCode);
    }

    public AlreadyInitializedException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
