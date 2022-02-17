package de.intelligence.drp.api;

public interface IDiscord extends Observer  {

    void registerEventHandler(Object obj);

    void unregisterEventHandler(Object obj);

    void setRichPresence(RichPresence presence);

    void connect();

    void disconnect();

}
