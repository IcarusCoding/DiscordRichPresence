package de.intelligence.drp.api;

public interface IDiscord extends Initializable, AutoCloseable {

    void registerEventHandler(Object obj);

    void unregisterEventHandler(Object obj);

    void setRichPresence(RichPresence presence);

}
