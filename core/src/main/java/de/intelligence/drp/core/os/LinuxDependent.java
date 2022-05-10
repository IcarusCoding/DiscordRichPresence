package de.intelligence.drp.core.os;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import de.intelligence.drp.api.exception.ErrorCode;
import de.intelligence.drp.core.exception.InitializationFailedException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

import java.util.List;
import java.util.Optional;

public class LinuxDependent implements OSDependent {

    private static final String LINUX_PIPE_PREFIX;

    static {
        LINUX_PIPE_PREFIX = Optional.ofNullable(System.getenv("XDG_RUNTIME_DIR"))
                .or(() -> Optional.ofNullable(System.getenv("TMP")))
                .orElse("/tmp") + "/";
    }

    protected final C c;

    public LinuxDependent() {
        this.c = Native.load("c", C.class);
    }

    @Override
    public int getCurrentPID() {
        return this.c.getpid();
    }

    @Override
    public PipeGenerator createPipeGenerator(String prefix, List<String> files) {
        return new IndependentPipeGenerator(prefix, files, LinuxPipe::new);
    }

    @Override
    public String getPipePrefix() {
        return LinuxDependent.LINUX_PIPE_PREFIX;
    }

    public class LinuxPipe implements Pipe {

        protected final String pipe;

        protected int fileDescriptor;

        public LinuxPipe(String pipe) {
            this.pipe = pipe;
        }

        @Override
        public void init() throws InitializationFailedException {
            this.fileDescriptor = LinuxDependent.this.c.socket(C.AF_UNIX, C.SOCK_STREAM, 0);
            if (this.fileDescriptor == -1) {
                throw new InitializationFailedException("Could not initialize socket connection to pipe \"" + this.pipe + "\".", ErrorCode.SOCKET_INIT_FAILED);
            }
            if (LinuxDependent.this.c.fcntl(this.fileDescriptor, C.F_SETFL, C.O_NONBLOCK) == -1) {
                throw new InitializationFailedException("Could not set file control flags for pipe \"" + this.pipe + "\": Native Error code: " + Native.getLastError(), ErrorCode.UNSPECIFIED);
            }
            final sockaddr_un sock = new sockaddr_un();
            sock.sa_family = (short) LinuxDependent.this.c.AF_UNIX;
            int idx = 0;
            for(char c : this.pipe.toCharArray()) {
                sock.sun_path[idx++] = (byte) c;
            }
            if (LinuxDependent.this.c.connect(this.fileDescriptor, sock, sock.size()) == -1) {
                throw new InitializationFailedException("Could not connect to unix socket of pipe \"" + this.pipe + "\": Native error code: " + Native.getLastError(), ErrorCode.UNSPECIFIED);
            }
        }

        @Override
        public void write(byte[] payload, int payloadSize) throws WriteFailureException {
            final Pointer bufPtr = new Memory(payloadSize);
            bufPtr.write(0, payload, 0, payloadSize);
            final NativeLong sent = LinuxDependent.this.c.send(this.fileDescriptor, bufPtr, payloadSize, 0);
            if(sent.longValue() < 0) {
                throw new WriteFailureException("Could not write to named pipe: Native error code: " + Native.getLastError(), ErrorCode.UNSPECIFIED);
            }
        }

        @Override
        public byte[] receive(int size) throws ReadFailureException {
            final Pointer bufPtr = new Memory(size);
            final NativeLong recv = LinuxDependent.this.c.recv(this.fileDescriptor, bufPtr, size, 0);
            if(recv.longValue() < 0) {
                throw new ReadFailureException("Could not read bytes from named pipe: Native error code " + Native.getLastError(), ErrorCode.UNSPECIFIED);
            }
            return bufPtr.getByteArray(0, size);
        }

        @Override
        public void close() {
            LinuxDependent.this.c.close(this.fileDescriptor);
        }

        @Override
        public String getPipeName() {
            return this.pipe;
        }

    }

    public interface C extends Library {

        int AF_UNIX = 1;
        int SOCK_STREAM = 1;
        int F_SETFL = 4;
        int O_NONBLOCK = 4;

        int getpid();

        int socket(int domain, int type, int protocol);

        int fcntl(int fildes, int cmd, int flags);

        int connect(int sockfd, sockaddr_un addr, int addrlen);

        NativeLong send(int sockfd, Pointer buf, int len, int flags);

        NativeLong recv(int sockfd, Pointer buf, int len, int flags);

        int close(int fd);

    }

    @Structure.FieldOrder({"sa_family", "sun_path"})
    public static class sockaddr_un extends Structure {

        public short sa_family;
        public byte[] sun_path = new byte[108];

        public static class ByReference extends sockaddr_un implements Structure.ByReference {}

    }

}
