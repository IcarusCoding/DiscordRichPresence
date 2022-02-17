package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class ReadFailureException extends ConnectionException {

    ReadFailureException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable, errorCode);
    }

    public ReadFailureException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
