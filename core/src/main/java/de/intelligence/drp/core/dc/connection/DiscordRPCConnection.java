package de.intelligence.drp.core.dc.connection;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import de.intelligence.drp.core.connection.AbstractRPCConnection;
import de.intelligence.drp.core.connection.IIPCConnection;
import de.intelligence.drp.core.connection.IRPCEventHandler;
import de.intelligence.drp.core.connection.RPCConnectionState;
import de.intelligence.drp.core.dc.DiscordConsts;
import de.intelligence.drp.core.dc.json.Message;
import de.intelligence.drp.core.dc.pojo.ConnectionInfo;
import de.intelligence.drp.core.dc.proto.Command;
import de.intelligence.drp.core.dc.proto.InternalEventType;
import de.intelligence.drp.core.dc.proto.Frame;
import de.intelligence.drp.core.dc.proto.Opcode;
import de.intelligence.drp.core.exception.ConnectionException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;

public final class DiscordRPCConnection extends AbstractRPCConnection<Message> {

    private final ConnectionInfo info;
    private final Gson gson;

    public DiscordRPCConnection(ConnectionInfo info, Gson gson, IIPCConnection ipcConnection) {
        super(ipcConnection);
        this.gson = gson;
        this.info = info;
    }

    @Override
    public void connect() throws ConnectionException {
        if (super.state == RPCConnectionState.CONNECT) {
            return;
        }
        if (!super.ipcConnection.isInitialized()) {
            super.ipcConnection.initialize();
        }
        if (!super.ipcConnection.isConnected()) {
            super.ipcConnection.connect();
        }
        switch (this.state) {
            case DISCONNECT -> this.doHandshake();
            case HANDSHAKE_DONE -> this.readHandshakeResponse();
            default -> {
            }
        }
    }

    @Override
    public void disconnect() {
        if (this.ipcConnection.isConnected()) {
            this.ipcConnection.disconnect();
            this.eventHandlers.forEach(IRPCEventHandler::onDisconnect);
        }
        this.state = RPCConnectionState.DISCONNECT;
    }

    @Override
    public void send(byte[] payload, int payloadSize) throws WriteFailureException {
        super.ipcConnection.send(payload, payloadSize);
    }

    @Override
    public byte[] receive(int size) throws ReadFailureException {
        Frame constructedFrame = null;
        while (true) {
            byte[] headerBuf = super.ipcConnection.receive(8);
            if (headerBuf.length == 0) {
                return null;
            }
            constructedFrame = Frame.fromByteArray(headerBuf);
            constructedFrame.setPayload(super.ipcConnection.receive(constructedFrame.getLength()));
            boolean shouldReturn = false;
            switch (constructedFrame.getOpcode()) { //TODO NPE when opcode is not registered
                case FRAME -> shouldReturn = true;
                default -> {
                }
            }
            if (shouldReturn) {
                return constructedFrame.getPayload();
            }
        }
    }

    public ConnectionInfo getConnectionInfo() {
        return this.info;
    }

    public Gson getGson() {
        return this.gson;
    }

    private void doHandshake() throws WriteFailureException {
        final byte[] payloadBuf = new byte[DiscordConsts.FRAME_LENGTH - 8]; // subtract header
        final byte[] source = this.gson.toJson(this.info).getBytes(StandardCharsets.UTF_8);
        System.arraycopy(source, 0, payloadBuf, 0, source.length);
        this.ipcConnection.send(new Frame(Opcode.HANDSHAKE, source.length, payloadBuf).toByteArray(), 8 + source.length);
        this.state = RPCConnectionState.HANDSHAKE_DONE;
    }

    private void readHandshakeResponse() throws ReadFailureException {
        final byte[] responseBuf = this.receive(0);
        if (responseBuf != null) {
            final Message message = this.gson.fromJson(new String(responseBuf), Message.class);
            if (message.getCommand() == Command.DISPATCH && message.getEvent() == InternalEventType.READY) {
                super.eventHandlers.forEach(e -> e.onConnect(message));
                this.state = RPCConnectionState.CONNECT;
            }
        }
    }

}
