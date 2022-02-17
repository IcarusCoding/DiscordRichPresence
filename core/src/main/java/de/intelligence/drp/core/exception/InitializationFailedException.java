package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class InitializationFailedException extends ConnectionException {

    public InitializationFailedException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable, errorCode);
    }

    public InitializationFailedException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
