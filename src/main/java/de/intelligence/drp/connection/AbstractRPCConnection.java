package de.intelligence.drp.connection;

import java.util.Objects;

public abstract non-sealed class AbstractRPCConnection implements IRPCConnection {

    protected final IIPCConnection ipcConnection;

    protected RPCConnectionState state;

    public AbstractRPCConnection(IIPCConnection ipcConnection) {
        this.ipcConnection = Objects.requireNonNull(ipcConnection, "An ipc connection instance must be specified.");
        this.state = RPCConnectionState.DISCONNECT;
    }

    @Override
    public abstract void connect();

    @Override
    public abstract void disconnect();

    @Override
    public void send(byte[] payload, int payloadSize) {

    }

    @Override
    public byte[] receive(int size) {
        return new byte[0];
    }

    @Override
    public boolean isConnected() {
        return this.state == RPCConnectionState.CONNECT;
    }

    @Override
    public RPCConnectionState getCurrentState() {
        return this.state;
    }

    @Override
    public IIPCConnection getIPCConnection() {
        return this.ipcConnection;
    }

}
