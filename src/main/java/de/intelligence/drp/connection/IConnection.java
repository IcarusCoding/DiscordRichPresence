package de.intelligence.drp.connection;

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
