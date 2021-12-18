package de.intelligence.drp.connection;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import de.intelligence.drp.exception.AlreadyInitializedException;
import de.intelligence.drp.exception.ConnectionFailureException;
import de.intelligence.drp.exception.InitializationFailedException;
import de.intelligence.drp.exception.ReadFailureException;
import de.intelligence.drp.exception.WriteFailureException;
import de.intelligence.drp.jna.Kernel32;

public final class IPCConnectionImpl implements IIPCConnection {

    private static final String WIN_PIPE_PREFIX = "\\\\.\\pipe\\";

    private final List<String> validPipes;

    private boolean initialized;
    private boolean connected;
    private String selectedPipe;
    private WinNT.HANDLE hNamedPipe;

    public IPCConnectionImpl() {
        this(new ArrayList<>());
    }

    public IPCConnectionImpl(List<String> validPipes) {
        this.validPipes = new ArrayList<>(validPipes);
    }

    @Override
    public void connect() {
        if (!this.initialized) {
            throw new IllegalStateException("Connection must be initialized first!");
        }
        if (this.connected) {
            return;
        }
        System.out.println(IPCConnectionImpl.WIN_PIPE_PREFIX + selectedPipe);
        this.hNamedPipe = Kernel32.INSTANCE.CreateFileA(IPCConnectionImpl.WIN_PIPE_PREFIX + selectedPipe,
                WinNT.GENERIC_READ | WinNT.GENERIC_WRITE, 0, null,
                WinNT.OPEN_EXISTING, 0, null);
        if (this.hNamedPipe != WinNT.INVALID_HANDLE_VALUE) {
            this.handleError(lastError -> {
                switch (lastError) {
                    case 0x02 -> throw new ConnectionFailureException("Could not find named pipe " + this.selectedPipe);
                    case 0x05 -> throw new ConnectionFailureException("Could not open named pipe: Access denied");
                    case 0xE7 -> throw new ConnectionFailureException("Could not open named pipe: Pipe is busy");
                    default -> throw new ConnectionFailureException("Could not open named pipe: Unknown error (" + lastError + ")");
                }
            });
        }
        this.connected = true;
    }

    @Override
    public void disconnect() {
        if (!this.connected) {
            return;
        }
        Kernel32.INSTANCE.CloseHandle(this.hNamedPipe);
        this.hNamedPipe = null;
        this.connected = false;
    }

    @Override
    public void send(byte[] payload, int payloadSize) {
        if (!this.connected) {
            throw new IllegalStateException("No connection was established!");
        }
        final IntByReference writtenRef = new IntByReference();
        if(!Kernel32.INSTANCE.WriteFile(this.hNamedPipe, payload, payloadSize, writtenRef, null)) {
            this.handleError(lastError -> {
                switch (lastError) {
                    default -> throw new WriteFailureException("Could not write to named pipe: Unknown error (" + lastError + ")");
                }
            });
            return;
        }
        if(writtenRef.getValue() != payloadSize) {
            throw new WriteFailureException("Failed to complete write operation: Byte mismatch [Expected: "
                    + payloadSize + ", Actual: " + writtenRef.getValue() + "]");
        }
    }

    @Override
    public byte[] receive(int size) {
        if (!this.connected) {
            throw new IllegalStateException("No connection was established!");
        }
        final IntByReference availableRef = new IntByReference();
        if(!Kernel32.INSTANCE.PeekNamedPipe(this.hNamedPipe, null, 0, null, availableRef, null)) {
            this.handleError(lastError -> {
                switch (lastError) {
                    default -> throw new ReadFailureException("Could not read available bytes from named pipe: Unknown error (" + lastError + ")");
                }
            });
            return new byte[0];
        }
        if(availableRef.getValue() != 0 && availableRef.getValue() >= size) {
            final byte[] payloadBuf = new byte[size];
            final IntByReference readRef = new IntByReference();
            if(!Kernel32.INSTANCE.ReadFile(this.hNamedPipe, payloadBuf, size, readRef, null)) {
                this.handleError(lastError -> {
                    switch (lastError) {
                        default -> throw new ReadFailureException("Could not read bytes from named pipe: Unknown error (" + lastError + ")");
                    }
                });
                if(readRef.getValue() != payloadBuf.length) {
                    throw new ReadFailureException("Failed to complete read operation: Byte mismatch [Expected: "
                            + payloadBuf.length + ", Actual: " + readRef.getValue() + "]");
                }
            } else {
                return payloadBuf;
            }
        }
        return new byte[0];
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setNamedPipes(List<String> pipes) {
        if (this.initialized) {
            throw new AlreadyInitializedException("Pipes must not be changed after initialization!");
        }
        this.validPipes.clear();
        this.validPipes.addAll(pipes.stream().map(p -> {
            final int idx = p.lastIndexOf("\\");
            return idx == -1 ? p : p.substring(idx + 1);
        }).toList());
    }

    @Override
    public List<String> getNamedPipes() {
        return Collections.unmodifiableList(this.validPipes);
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void initialize() {
        if (this.initialized) {
            throw new AlreadyInitializedException("Connection must only be initialized once!");
        }
        if (this.validPipes.isEmpty()) {
            throw new IllegalStateException("At least one named pipe has to be defined before initialization!");
        }
        String selected = null;
        boolean found = false;
        for (final String name : this.validPipes) {
            try {
                new RandomAccessFile(IPCConnectionImpl.WIN_PIPE_PREFIX + name, "rw");
                selected = name;
                found = true;
            } catch (FileNotFoundException ignored) {}
        }
        if (!found) {
            throw new InitializationFailedException("Could not reserve a named pipe!");
        }
        this.selectedPipe = selected;
        this.initialized = true;
    }

    private void handleError(Consumer<Integer> errorConsumer) {
        final int lastError = Kernel32.INSTANCE.GetLastError();
        if(lastError != 0) {
            errorConsumer.accept(lastError);
        }
    }

}
