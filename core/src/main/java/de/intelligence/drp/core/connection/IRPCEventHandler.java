package de.intelligence.drp.core.connection;

public interface IRPCEventHandler<T> {

    void onConnect(T response);

    void onDisconnect();

}
