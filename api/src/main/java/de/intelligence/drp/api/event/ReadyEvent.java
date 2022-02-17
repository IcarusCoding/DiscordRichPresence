package de.intelligence.drp.api.event;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.annotation.EventMetadata;
import de.intelligence.drp.api.user.IDiscordUser;

@EventMetadata(eventType = EventType.READY)
public final class ReadyEvent implements DiscordEvent {

    private final IDiscord discord;
    private final IDiscordUser discordUser;

    public ReadyEvent(IDiscord discord, IDiscordUser discordUser) {
        this.discord = discord;
        this.discordUser = discordUser;
    }

    public IDiscord getDiscord() {
        return this.discord;
    }

    public IDiscordUser getDiscordUser() {
        return this.discordUser;
    }

}
