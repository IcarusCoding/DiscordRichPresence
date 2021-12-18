package de.intelligence.drp;

import de.intelligence.drp.dc.DiscordWrapper;

public final class Test {

    public static void main(String[] args) {
       /* final IIPCConnection conn = new IPCConnectionImpl();
        conn.setNamedPipes(Collections.singletonList("\\\\.\\pipe\\discord-ipc-0"));
        final IRPCConnection rpcConn = new DiscordRPCConnection(new ConnectionInfo(1, "918252911631400970"), conn);
        rpcConn.connect(); */

        final DiscordWrapper wrapper = new DiscordWrapper("918252911631400970");
        wrapper.initialize();

    }

}
