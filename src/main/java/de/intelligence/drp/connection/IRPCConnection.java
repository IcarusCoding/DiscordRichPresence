package de.intelligence.drp.connection;

public sealed interface IRPCConnection<T> extends IConnection permits AbstractRPCConnection {

    RPCConnectionState getCurrentState();

    IIPCConnection getIPCConnection();

    void addEventHandler(IRPCEventHandler<T> eventHandler);

}
