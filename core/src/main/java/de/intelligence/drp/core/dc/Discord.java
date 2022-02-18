package de.intelligence.drp.core.dc;

import de.intelligence.drp.api.IDiscord;

public final class Discord {

    public static IDiscord create(String applicationId) {
        return new DiscordWrapper(applicationId);
    }

}
