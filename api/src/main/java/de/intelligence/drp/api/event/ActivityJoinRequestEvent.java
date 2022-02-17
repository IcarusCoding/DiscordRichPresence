package de.intelligence.drp.api.event;

import de.intelligence.drp.api.annotation.EventMetadata;

@Deprecated
@EventMetadata(needsSubscription = true, eventType = EventType.ACTIVITY_JOIN_REQUEST)
public final class ActivityJoinRequestEvent implements DiscordEvent {}
