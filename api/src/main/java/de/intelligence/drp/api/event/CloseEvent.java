package de.intelligence.drp.api.event;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.annotation.EventMetadata;

@EventMetadata
public record CloseEvent(IDiscord discord) implements DiscordEvent {}
