package de.intelligence.drp.api.event;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.annotation.EventMetadata;
import de.intelligence.drp.api.user.IDiscordUser;

@EventMetadata(eventType = EventType.READY)
public record ReadyEvent(IDiscord discord, IDiscordUser discordUser) implements DiscordEvent {}
