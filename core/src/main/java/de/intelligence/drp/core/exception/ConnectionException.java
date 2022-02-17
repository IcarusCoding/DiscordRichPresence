package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public class ConnectionException extends Exception {

    private final ErrorCode errorCode;

    ConnectionException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable);
        this.errorCode = errorCode;
    }

    ConnectionException(String msg, ErrorCode errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

}
