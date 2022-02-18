package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class ConnectionFailureException extends ConnectionException {

    public ConnectionFailureException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
