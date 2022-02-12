package de.intelligence.drp.connection;

public interface IRPCEventHandler<T> {

    void onConnect(T response);

    void onDisconnect();

}
