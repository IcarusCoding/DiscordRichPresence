package de.intelligence.drp.core.connection;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import de.intelligence.drp.core.exception.*;
import de.intelligence.drp.api.exception.ErrorCode;
import de.intelligence.drp.core.os.OSDependent;
import de.intelligence.drp.core.os.OSUtils;

public final class IPCConnectionImpl implements IIPCConnection {

    private final List<String> validPipes;
    private final OSDependent osDependent;

    private boolean initialized;
    private boolean connected;
    private OSDependent.Pipe pipe;

    public IPCConnectionImpl() {
        this(new ArrayList<>());
    }

    public IPCConnectionImpl(List<String> validPipes) {
        this.validPipes = new ArrayList<>(validPipes);
        this.osDependent = OSUtils.getOsDependent();
    }

    @Override
    public void connect() throws ConnectionException {
        if (!this.initialized) {
            throw new IllegalStateException("Connection must be initialized first!");
        }
        if (this.connected) {
            return;
        }
        this.pipe = this.osDependent.createPipeGenerator(this.osDependent.getPipePrefix(), this.validPipes).generate();
        this.pipe.init();
        this.connected = true;
    }

    @Override
    public void disconnect() {
        if (!this.connected) {
            return;
        }
        this.pipe.close();
        this.pipe = null;
        this.connected = false;
    }

    @Override
    public void send(byte[] payload, int payloadSize) throws WriteFailureException {
        if (!this.connected) {
            throw new IllegalStateException("No connection was established!");
        }
        this.pipe.write(payload, payloadSize);
    }

    @Override
    public byte[] receive(int size) throws ReadFailureException {
        if (!this.connected) {
            throw new IllegalStateException("No connection was established!");
        }
        return this.pipe.receive(size);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setNamedPipes(List<String> pipes) {
        if (this.initialized) {
            return;
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
    public void initialize() throws InitializationFailedException, AlreadyInitializedException {
        if (this.initialized) {
            throw new AlreadyInitializedException("Connection must only be initialized once!", ErrorCode.ALREADY_INITIALIZED);
        }
        if (this.validPipes.isEmpty()) {
            throw new InitializationFailedException("At least one named pipe has to be defined before initialization!", ErrorCode.UNSPECIFIED);
        }
        this.initialized = true;
    }

}
