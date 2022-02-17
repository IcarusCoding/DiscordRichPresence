package de.intelligence.drp.api;

import de.intelligence.drp.api.event.EventType;

public interface Subscribable {

    void subscribe(EventType type);

    void unsubscribe(EventType type);

}
