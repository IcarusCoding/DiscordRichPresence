package de.intelligence.drp.core.os;

import de.intelligence.drp.api.exception.ErrorCode;
import de.intelligence.drp.core.exception.GeneratorException;
import de.intelligence.drp.core.exception.InitializationFailedException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface OSDependent {

    int getCurrentPID();

    PipeGenerator createPipeGenerator(String prefix, List<String> files);

    String getPipePrefix();

    interface Pipe {

        void init() throws InitializationFailedException;

        void write(byte[] payload, int payloadSize) throws WriteFailureException;

        byte[] receive(int size) throws ReadFailureException;

        void close();

        String getPipeName();

    }

    class IndependentPipeGenerator implements PipeGenerator {

        protected final String prefix;
        protected final List<String> files;
        protected final Function<String, Pipe> pipeCreatorFunc;

        protected IndependentPipeGenerator(String prefix, List<String> files, Function<String, Pipe> pipeCreatorFunc) {
            this.prefix = prefix;
            this.files = Collections.unmodifiableList(files);
            this.pipeCreatorFunc = pipeCreatorFunc;
        }

        @Override
        public Pipe generate() throws GeneratorException {
            final List<Throwable> exceptions = new ArrayList<>();
            for(final String file : this.files) {
                final Pipe pipe = this.pipeCreatorFunc.apply(this.prefix + file);
                try {
                    pipe.init();
                    return pipe;
                } catch (InitializationFailedException ex) {
                    exceptions.add(ex);
                }
            }
            throw new GeneratorException("Failed to create pipe from supplied file possibilities", ErrorCode.GENERATOR_FAILED, exceptions);
        }

    }

    interface PipeGenerator {

        Pipe generate() throws GeneratorException;

    }

}
