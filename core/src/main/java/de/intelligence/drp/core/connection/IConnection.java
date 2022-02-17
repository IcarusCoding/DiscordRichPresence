package de.intelligence.drp.core.connection;

import de.intelligence.drp.core.exception.ConnectionException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

public sealed interface IConnection extends AutoCloseable permits IIPCConnection, IRPCConnection {

    void connect() throws ConnectionException;

    void disconnect();

    void send(byte[] payload, int payloadSize) throws WriteFailureException;

    byte[] receive(int size) throws ReadFailureException;

    boolean isConnected();

    @Override
    default void close() throws Exception {
        this.disconnect();
    }

}
