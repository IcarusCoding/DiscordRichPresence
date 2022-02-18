package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class ReadFailureException extends ConnectionException {

    public ReadFailureException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
