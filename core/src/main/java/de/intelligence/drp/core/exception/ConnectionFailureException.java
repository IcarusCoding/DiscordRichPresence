package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class ConnectionFailureException extends ConnectionException {

    ConnectionFailureException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable, errorCode);
    }

    public ConnectionFailureException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
