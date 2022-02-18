package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

public final class InitializationFailedException extends ConnectionException {

    public InitializationFailedException(String msg, ErrorCode errorCode) {
        super(msg, errorCode);
    }

}
