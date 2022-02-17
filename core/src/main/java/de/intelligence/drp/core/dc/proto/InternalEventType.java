package de.intelligence.drp.core.dc.proto;

import com.google.common.base.Enums;

import de.intelligence.drp.api.event.EventType;

public enum InternalEventType {

    NONE,
    ERROR,
    INVALID,
    READY,
    ACTIVITY_JOIN,
    ACTIVITY_JOIN_REQUEST,
    ACTIVITY_SPECTATE;

    public static InternalEventType fromEventType(EventType type) {
        return Enums.getIfPresent(InternalEventType.class, type.name()).or(InternalEventType.NONE);
    }

}
