package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class WriteFailureException extends ConnectionException {

    WriteFailureException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable, errorCode);
    }

    public WriteFailureException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
