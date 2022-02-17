package de.intelligence.drp.core.connection;

public sealed interface IRPCConnection<T> extends IConnection permits AbstractRPCConnection {

    RPCConnectionState getCurrentState();

    IIPCConnection getIPCConnection();

    void addEventHandler(IRPCEventHandler<T> eventHandler);

    void removeEventHandler(IRPCEventHandler<T> eventHandler);

}
