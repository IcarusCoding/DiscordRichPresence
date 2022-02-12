package de.intelligence.drp.dc.api;

import de.intelligence.drp.dc.proto.Event;

public interface DiscordRichPresence {

    void subscribe(Event event);

    void unsubscribe(Event event);

}
