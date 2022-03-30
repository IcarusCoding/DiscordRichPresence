package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public class ConnectionException extends ErrorCodeException {

    ConnectionException(String msg, Throwable throwable, ErrorCode errorCode) {
        super(msg, throwable, errorCode);
    }

    ConnectionException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
