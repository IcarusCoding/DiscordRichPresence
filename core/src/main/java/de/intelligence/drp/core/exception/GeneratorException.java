package de.intelligence.drp.core.exception;

import de.intelligence.drp.api.exception.ErrorCode;

import java.util.List;

public class GeneratorException extends ConnectionException {

    public GeneratorException(String msg, ErrorCode errorCode, List<Throwable> suppressed) {
        super(msg, errorCode);
        suppressed.forEach(super::addSuppressed);
    }

}
