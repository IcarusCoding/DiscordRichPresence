package de.intelligence.drp.core.os;

import com.sun.jna.Library;
import com.sun.jna.Native;
import de.intelligence.drp.core.exception.ConnectionFailureException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

public final class LinuxDependent implements OSDependent {

    private final C c;

    public LinuxDependent() {
        this.c = Native.load("c", C.class);
    }

    @Override
    public int getCurrentPID() {
        return this.c.getpid();
    }

    @Override
    public Pipe createPipe(String file) {
        return new LinuxPipe();
    }

    @Override
    public String getPipePrefix() {
        throw new UnsupportedOperationException();
    }

    public class LinuxPipe implements Pipe {

        @Override
        public boolean isInvalid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(byte[] payload, int payloadSize) throws WriteFailureException {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] receive(int size) throws ReadFailureException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkError() throws ConnectionFailureException {
            throw new UnsupportedOperationException();
        }

    }

    public interface C extends Library {

        int getpid();

    }

}
