package de.intelligence.drp.api;

import de.intelligence.drp.api.event.EventType;

public interface Subscriptable {

    void subscribe(EventType type);

    void unsubscribe(EventType type);

}
