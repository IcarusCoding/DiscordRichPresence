package de.intelligence.drp.api.event;

import de.intelligence.drp.api.annotation.EventMetadata;

@Deprecated
@EventMetadata(needsSubscription = true, eventType = EventType.ACTIVITY_SPECTATE)
public final class ActivitySpectateEvent implements DiscordEvent {}
