package de.intelligence.drp.dc.connection;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import de.intelligence.drp.connection.AbstractRPCConnection;
import de.intelligence.drp.connection.IIPCConnection;
import de.intelligence.drp.connection.RPCConnectionState;
import de.intelligence.drp.dc.pojo.ConnectionInfo;
import de.intelligence.drp.dc.proto.Frame;
import de.intelligence.drp.dc.proto.Opcode;

public final class DiscordRPCConnection extends AbstractRPCConnection {

    private static final int FRAME_LENGTH = 32 * 1024;

    private final ConnectionInfo info;
    private final Gson gson;

    public DiscordRPCConnection(ConnectionInfo info, IIPCConnection ipcConnection) {
        super(ipcConnection);
        this.gson = new Gson();
        this.info = info;
    }

    @Override
    public void connect() {
        System.out.println("TRYING TO CONNECT");
        if(super.state == RPCConnectionState.CONNECT) {
            return;
        }
        if(!super.ipcConnection.isInitialized()) {
            super.ipcConnection.initialize();
        }
        if(!super.ipcConnection.isConnected()) {
            super.ipcConnection.connect();
        }
        switch(this.state) {
            case DISCONNECT -> this.doHandshake();
            case HANDSHAKE_DONE -> this.readHandshakeResponse();
            default -> {}
        }
    }

    @Override
    public void disconnect() {
        if(this.ipcConnection.isConnected()) {
            this.ipcConnection.disconnect();
        }
        this.state = RPCConnectionState.DISCONNECT;
    }

    public ConnectionInfo getConnectionInfo() {
        return this.info;
    }

    private void doHandshake() {
        final byte[] payloadBuf = new byte[FRAME_LENGTH - 8]; // subtract header
        final byte[] source = this.gson.toJson(this.info).getBytes(StandardCharsets.UTF_8);
        System.arraycopy(source, 0, payloadBuf, 0, source.length);
        this.ipcConnection.send(new Frame(Opcode.HANDSHAKE, source.length, payloadBuf).toByteArray(), 8 + source.length);
    }

    private void readHandshakeResponse() {

    }

}
