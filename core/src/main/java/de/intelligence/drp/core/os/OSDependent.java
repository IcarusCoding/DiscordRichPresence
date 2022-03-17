package de.intelligence.drp.core.os;

import de.intelligence.drp.core.exception.ConnectionFailureException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

public interface OSDependent {

    int getCurrentPID();

    Pipe createPipe(String file);

    String getPipePrefix();

    interface Pipe {

        boolean isInvalid();

        void write(byte[] payload, int payloadSize) throws WriteFailureException;

        byte[] receive(int size) throws ReadFailureException;

        void close();

        void checkError() throws ConnectionFailureException;

    }

}
