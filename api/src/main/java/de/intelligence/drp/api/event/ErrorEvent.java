package de.intelligence.drp.api.event;

import de.intelligence.drp.api.annotation.EventMetadata;
import de.intelligence.drp.api.exception.ErrorCode;

@EventMetadata
public record ErrorEvent(Throwable exception, ErrorCode errorCode) implements DiscordEvent {

    public String getErrorMessage() {
        return this.exception.getMessage();
    }

}
