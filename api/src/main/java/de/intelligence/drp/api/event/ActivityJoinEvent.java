package de.intelligence.drp.api.event;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.annotation.EventMetadata;

@EventMetadata(eventType = EventType.ACTIVITY_JOIN, needsSubscription = true)
public final class ActivityJoinEvent implements DiscordEvent {

    private final IDiscord discord;
    private final String secret;

    public ActivityJoinEvent(IDiscord discord, String secret) {
        this.discord = discord;
        this.secret = secret;
    }

    public IDiscord getDiscord() {
        return this.discord;
    }

    public String getSecret() {
        return this.secret;
    }

}
