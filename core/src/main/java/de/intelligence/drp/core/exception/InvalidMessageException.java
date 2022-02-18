package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class InvalidMessageException extends ConnectionException {

    public InvalidMessageException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
