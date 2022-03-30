package de.intelligence.drp.core.os;

import de.intelligence.drp.core.exception.InitializationFailedException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

import java.util.List;

public final class MacDependent implements OSDependent {

    @Override
    public int getCurrentPID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PipeGenerator createPipeGenerator(String prefix, List<String> files) {
        return new IndependentPipeGenerator(prefix, files, MacPipe::new);
    }

    @Override
    public String getPipePrefix() {
        throw new UnsupportedOperationException();
    }

    public class MacPipe implements Pipe {

        private final String pipe;

        public MacPipe(String pipe) {
            this.pipe = pipe;
        }

        @Override
        public void init() throws InitializationFailedException {
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
        public String getPipeName() {
            throw new UnsupportedOperationException();
        }

    }

}
