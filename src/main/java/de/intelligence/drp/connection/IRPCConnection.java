package de.intelligence.drp.connection;

public sealed interface IRPCConnection extends IConnection permits AbstractRPCConnection {

    RPCConnectionState getCurrentState();

    IIPCConnection getIPCConnection();

}
