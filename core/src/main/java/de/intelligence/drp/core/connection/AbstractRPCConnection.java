package de.intelligence.drp.core.connection;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract non-sealed class AbstractRPCConnection<T> implements IRPCConnection<T> {

    protected final IIPCConnection ipcConnection;

    protected RPCConnectionState state;
    protected Set<IRPCEventHandler<T>> eventHandlers;

    public AbstractRPCConnection(IIPCConnection ipcConnection) {
        this.ipcConnection = Objects.requireNonNull(ipcConnection, "An ipc connection instance must be specified.");
        this.state = RPCConnectionState.DISCONNECT;
        this.eventHandlers = new HashSet<>();
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

    @Override
    public void addEventHandler(IRPCEventHandler<T> eventHandler) {
        this.eventHandlers.add(eventHandler);
    }

    @Override
    public void removeEventHandler(IRPCEventHandler<T> eventHandler) {
        this.eventHandlers.remove(eventHandler);
    }

}
