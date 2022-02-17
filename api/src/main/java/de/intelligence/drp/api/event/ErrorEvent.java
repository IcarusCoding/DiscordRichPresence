package de.intelligence.drp.api.event;

import de.intelligence.drp.api.annotation.EventMetadata;
import de.intelligence.drp.api.exception.ErrorCode;

@EventMetadata
public final class ErrorEvent implements DiscordEvent {

    private final Throwable exception;
    private final ErrorCode errorCode;

    public ErrorEvent(Throwable exception, ErrorCode errorCode) {
        this.exception = exception;
        this.errorCode = errorCode;
    }

    public Throwable getException() {
        return this.exception;
    }

    public String getErrorMessage() {
        return this.exception.getMessage();
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

}
