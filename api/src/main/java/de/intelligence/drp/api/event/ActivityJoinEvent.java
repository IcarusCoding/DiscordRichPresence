package de.intelligence.drp.api.event;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.annotation.EventMetadata;

@EventMetadata(eventType = EventType.ACTIVITY_JOIN, needsSubscription = true)
public record ActivityJoinEvent(IDiscord discord, String secret) implements DiscordEvent {}
