package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public class ErrorCodeException extends Exception {

    private final ErrorCode errorCode;

    protected ErrorCodeException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable);
        this.errorCode = errorCode;
    }

    protected ErrorCodeException(String msg, ErrorCode errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

}
