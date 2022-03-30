package de.intelligence.drp.core.os;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import de.intelligence.drp.api.exception.ErrorCode;
import de.intelligence.drp.core.exception.InitializationFailedException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

import java.util.List;

public final class WindowsDependent implements OSDependent {

    private static final String WIN_PIPE_PREFIX = "\\\\.\\pipe\\";

    private final Kernel32 kernel32;

    public WindowsDependent() {
        this.kernel32 = Native.load("kernel32", Kernel32.class);
    }

    public Kernel32 getKernel32() {
        return this.kernel32;
    }

    @Override
    public int getCurrentPID() {
        return this.kernel32.GetCurrentProcessId();
    }

    @Override
    public PipeGenerator createPipeGenerator(String prefix, List<String> files) {
        return new IndependentPipeGenerator(prefix, files, WinPipe::new);
    }

    @Override
    public String getPipePrefix() {
        return WindowsDependent.WIN_PIPE_PREFIX;
    }

    public class WinPipe implements Pipe {

        private final String pipe;

        private WinNT.HANDLE hPipe;

        public WinPipe(String pipe) {
            this.pipe = pipe;
        }

        @Override
        public void init() throws InitializationFailedException {
            this.hPipe = WindowsDependent.this.kernel32.CreateFileA(this.pipe, WinNT.GENERIC_READ | WinNT.GENERIC_WRITE,
                    0, null, WinNT.OPEN_EXISTING, 0, null);
            if (this.hPipe == WinNT.INVALID_HANDLE_VALUE) {
                throw new InitializationFailedException("Invalid handle received while creating pipe \"" + this.pipe + "\".", ErrorCode.INVALID_HANDLE);
            }
            final int lastError = WindowsDependent.this.kernel32.GetLastError();
            if(lastError != 0) {
                switch (lastError) {
                    case 0x02 -> throw new InitializationFailedException("Could not find named pipe " + this.pipe, ErrorCode.PIPE_NOT_FOUND);
                    case 0x05 -> throw new InitializationFailedException("Could not open named pipe " + this.pipe + ": Access denied", ErrorCode.ACCESS_DENIED);
                    case 0xE7 -> throw new InitializationFailedException("Could not open named pipe " + this.pipe + ": Pipe is busy", ErrorCode.PIPE_BUSY);
                    default -> throw new InitializationFailedException("Could not open named pipe " + this.pipe + ": Native error code " + lastError, ErrorCode.UNSPECIFIED);
                }
            }
        }

        @Override
        public void write(byte[] payload, int payloadSize) throws WriteFailureException {
            final IntByReference writtenRef = new IntByReference();
            if (!WindowsDependent.this.kernel32.WriteFile(this.hPipe, payload, payloadSize, writtenRef, null)) {
                final int lastError = WindowsDependent.this.kernel32.GetLastError();
                throw new WriteFailureException("Could not write to named pipe: Native error code: " + lastError, ErrorCode.UNSPECIFIED);
            }
            if (writtenRef.getValue() != payloadSize) {
                throw new WriteFailureException("Failed to complete write operation: Byte mismatch [Expected: "
                        + payloadSize + ", Actual: " + writtenRef.getValue() + "]", ErrorCode.BYTE_MISMATCH);
            }
        }

        @Override
        public byte[] receive(int size) throws ReadFailureException {
            final IntByReference availableRef = new IntByReference();
            if (!WindowsDependent.this.kernel32.PeekNamedPipe(this.hPipe, null, 0, null, availableRef, null)) {
                final int lastError = WindowsDependent.this.kernel32.GetLastError();
                if (lastError == 0x6D) {
                    throw new ReadFailureException("Could not read available bytes from named pipe: Pipe ended", ErrorCode.PIPE_ENDED);
                }
                throw new ReadFailureException("Could not read available bytes from named pipe: Native error code " + lastError, ErrorCode.UNSPECIFIED);
            }
            if (availableRef.getValue() != 0 && availableRef.getValue() >= size) {
                final byte[] payloadBuf = new byte[size];
                final IntByReference readRef = new IntByReference();
                if (!WindowsDependent.this.kernel32.ReadFile(this.hPipe, payloadBuf, size, readRef, null)) {
                    final int lastError = WindowsDependent.this.kernel32.GetLastError();
                    if (lastError != 0) {
                        throw new ReadFailureException("Could not read bytes from named pipe: Native error code " + lastError, ErrorCode.UNSPECIFIED);
                    }
                    if (readRef.getValue() != payloadBuf.length) {
                        throw new ReadFailureException("Failed to complete read operation: Byte mismatch [Expected: "
                                + payloadBuf.length + ", Actual: " + readRef.getValue() + "]", ErrorCode.BYTE_MISMATCH);
                    }
                } else {
                    return payloadBuf;
                }
            }
            return new byte[0];
        }

        @Override
        public void close() {
            WindowsDependent.this.kernel32.CloseHandle(this.hPipe);
        }

        @Override
        public String getPipeName() {
            return this.pipe;
        }

    }


    public interface Kernel32 extends StdCallLibrary {

        WinNT.HANDLE CreateFileA(String lpFileName, int dwDesiredAccess, int dwShareMode, WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes, WinNT.HANDLE hTemplateFile);

        boolean ReadFile(WinNT.HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead, WinBase.OVERLAPPED lpOverlapped);

        boolean WriteFile(WinNT.HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToWrite, IntByReference lpNumberOfBytesWritten, WinBase.OVERLAPPED lpOverlapped);

        boolean CloseHandle(WinNT.HANDLE hObject);

        boolean PeekNamedPipe(WinNT.HANDLE hNamedPipe, byte[] lpBuffer, int nBufferSize, IntByReference lpBytesRead, IntByReference lpTotalBytesAvail, IntByReference lpBytesLeftThisMessage);

        int GetLastError();

        int GetCurrentProcessId();

    }

}
