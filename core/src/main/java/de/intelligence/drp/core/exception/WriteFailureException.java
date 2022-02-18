package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class WriteFailureException extends ConnectionException {

    public WriteFailureException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
