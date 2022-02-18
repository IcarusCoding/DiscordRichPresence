package de.intelligence.drp.api.event;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.annotation.EventMetadata;

@Deprecated
@EventMetadata(needsSubscription = true, eventType = EventType.ACTIVITY_SPECTATE)
public record ActivitySpectateEvent(IDiscord discord) implements DiscordEvent {}
