package de.intelligence.drp.core.connection;

public sealed interface IConnection extends AutoCloseable permits IIPCConnection, IRPCConnection {

    void connect();

    void disconnect();

    void send(byte[] payload, int payloadSize);

    byte[] receive(int size);

    boolean isConnected();

    @Override
    default void close() throws Exception {
        this.disconnect();
    }

}
