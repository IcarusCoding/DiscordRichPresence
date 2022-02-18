package de.intelligence.drp.api.event;

import de.intelligence.drp.api.annotation.EventMetadata;

@EventMetadata
public record CloseEvent() implements DiscordEvent {}
