package de.intelligence.drp.core.os;

import com.sun.jna.Native;
import de.intelligence.drp.api.exception.ErrorCode;
import de.intelligence.drp.core.exception.InitializationFailedException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

import java.util.List;
import java.util.Optional;

public final class MacDependent extends LinuxDependent {

    private static final String MAC_PIPE_PREFIX;

    static {
        MAC_PIPE_PREFIX = Optional.ofNullable(System.getenv("TMPDIR"))
                .orElse("/tmp") + "/";
    }

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
        return MAC_PIPE_PREFIX;
    }

    public class MacPipe extends LinuxPipe {

        public MacPipe(String pipe) {
            super(pipe);
        }

        @Override
        public void init() throws InitializationFailedException {
            super.fileDescriptor = MacDependent.this.c.socket(C.AF_UNIX, C.SOCK_STREAM, 0);
            if (this.fileDescriptor == -1) {
                throw new InitializationFailedException("Could not initialize socket connection to pipe \"" + this.pipe + "\".", ErrorCode.SOCKET_INIT_FAILED);
            }
            final sockaddr_un sock = new sockaddr_un();
            sock.sa_family = (short) MacDependent.this.c.AF_UNIX;
            int idx = 0;
            for(char c : this.pipe.toCharArray()) {
                sock.sun_path[idx++] = (byte) c;
            }
            if (MacDependent.this.c.connect(this.fileDescriptor, sock, sock.size()) == -1) {
                throw new InitializationFailedException("Could not connect to unix socket of pipe \"" + this.pipe + "\": Native error code: " + Native.getLastError(), ErrorCode.UNSPECIFIED);
            }
        }

    }

}
