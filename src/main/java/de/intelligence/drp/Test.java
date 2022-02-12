package de.intelligence.drp;

import de.intelligence.drp.connection.IRPCEventHandler;
import de.intelligence.drp.dc.DiscordWrapper;
import de.intelligence.drp.dc.json.Message;
import de.intelligence.drp.dc.proto.Event;

public final class Test {

    public static void main(String[] args) throws InterruptedException {
       /* final IIPCConnection conn = new IPCConnectionImpl();
        conn.setNamedPipes(Collections.singletonList("\\\\.\\pipe\\discord-ipc-0"));
        final IRPCConnection rpcConn = new DiscordRPCConnection(new ConnectionInfo(1, "918252911631400970"), conn);
        rpcConn.connect(); */

        final DiscordWrapper wrapper = new DiscordWrapper("918252911631400970");
        wrapper.addEventHandler(new IRPCEventHandler<Message>() {
            @Override
            public void onConnect(Message message) {
                System.out.println("CONNECTED -> " + message);
            }

            @Override
            public void onDisconnect() {
                System.out.println("DISCONNECTED");
            }
        });
        new Thread(wrapper::initialize).start();
        Thread.sleep(1000);
        wrapper.subscribe(Event.ACTIVITY_JOIN);
        Thread.sleep(Integer.MAX_VALUE);
    }

}
