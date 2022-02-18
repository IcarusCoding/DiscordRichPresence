package de.intelligence.drp.api;

import java.util.Optional;

import de.intelligence.drp.api.user.IDiscordUser;

public interface IDiscord extends Observer, Subscribable {

    void registerEventHandler(Object obj);

    void unregisterEventHandler(Object obj);

    void setRichPresence(RichPresence presence);

    void connect();

    void connectAsync();

    void disconnect();

    Optional<IDiscordUser> getConnectedUser();

    String getApplicationId();

}
