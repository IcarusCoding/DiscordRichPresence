package de.intelligence.drp.dc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import de.intelligence.drp.Initializable;
import de.intelligence.drp.connection.IIPCConnection;
import de.intelligence.drp.connection.IPCConnectionImpl;
import de.intelligence.drp.connection.IRPCConnection;
import de.intelligence.drp.connection.IRPCEventHandler;
import de.intelligence.drp.dc.api.DiscordRichPresence;
import de.intelligence.drp.dc.connection.DiscordRPCConnection;
import de.intelligence.drp.dc.json.Message;
import de.intelligence.drp.dc.json.MessageDeserializer;
import de.intelligence.drp.dc.pojo.ConnectionInfo;
import de.intelligence.drp.dc.pojo.DiscordUser;
import de.intelligence.drp.dc.proto.Command;
import de.intelligence.drp.dc.proto.Event;
import de.intelligence.drp.dc.proto.Frame;
import de.intelligence.drp.dc.proto.Opcode;

import static de.intelligence.drp.dc.connection.DiscordRPCConnection.FRAME_LENGTH;

public final class DiscordWrapper implements DiscordRichPresence, Initializable, IRPCEventHandler<Message> {

    private static final int MAX_ACTIVITY_WAIT = 1000;

    private final String applicationId;
    private final ReentrantLock reentrantLock;
    private final Condition activityCondition;
    private final IRPCConnection<Message> rpcConnection;
    private final Supplier<Boolean> retryFunc;
    private final Gson gson;
    private final AtomicBoolean wasConnectedRecently;
    private final AtomicBoolean wasDisconnectedRecently;
    private final Queue<Frame> queueOut;

    private boolean initialized;
    private boolean aborted;
    private DiscordUser connectedUser;
    private int nonce;

    public DiscordWrapper(String applicationId) {
        this.applicationId = applicationId;
        this.reentrantLock = new ReentrantLock();
        this.activityCondition = this.reentrantLock.newCondition();
        final IIPCConnection conn = new IPCConnectionImpl();
        this.gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageDeserializer()).create();
        this.wasConnectedRecently = new AtomicBoolean();
        this.wasDisconnectedRecently = new AtomicBoolean();
        this.queueOut = new LinkedBlockingQueue<>();
        conn.setNamedPipes(new ArrayList<>(Arrays.asList("\\\\.\\pipe\\discord-ipc-0", "\\\\.\\pipe\\discord-ipc-1", "\\\\.\\pipe\\discord-ipc-2")));
        this.rpcConnection = new DiscordRPCConnection(new ConnectionInfo(1, this.applicationId), this.gson, conn);
        this.retryFunc = Retry.decorateSupplier(Retry.of("initConn", RetryConfig.<Boolean>custom().maxAttempts(5)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(500, IntervalFunction.DEFAULT_MULTIPLIER))
                .retryOnResult(a -> a).build()), () -> {
            this.rpcConnection.connect();
            return !this.rpcConnection.isConnected();
        });
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void initialize() {
        if (this.initialized) {
            return;
        }
        this.rpcConnection.addEventHandler(this);
        this.initialized = true;
        this.updateConnection();
        while (!this.aborted) {
            this.reentrantLock.lock();
            try {
                this.activityCondition.await(MAX_ACTIVITY_WAIT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }
            this.updateConnection();
        }
    }

    public void addEventHandler(IRPCEventHandler<Message> eventHandler) {
        this.rpcConnection.addEventHandler(eventHandler);
    }

    private void updateConnection() {
        System.out.println("UPDATE CONN");
        if (!this.rpcConnection.isConnected()) {
            System.out.println("INIT CONN");
            this.retryFunc.get();
            return;
        }
        while (true) {
            final byte[] readBuf = this.rpcConnection.receive(0);
            if (readBuf == null) {
                break;
            }
            final Message message = this.gson.fromJson(new String(readBuf), Message.class);
            if (message.hasNonce()) {
                System.out.println("RECV: " + message);
                if(message.getEvent() == Event.ERROR) {
                    System.out.println("ERROR");
                }
            } else {
                System.out.println("NO NONCE <---------- TODO");
            }
        }
        while(!this.queueOut.isEmpty()) {
            final Frame frameOut = this.queueOut.poll();
            this.rpcConnection.send(frameOut.toByteArray(), frameOut.getFullLength());
        }
    }

    @Override
    public void subscribe(Event event) {
        final String json = createJson(Command.SUBSCRIBE, event, this.nonce++);
        System.out.println("JSON TO SEND: " + json);
        final Frame subscribeFrame = this.createFrame(json);
        this.queueOut.add(subscribeFrame);
    }

    @Override
    public void unsubscribe(Event event) {
        final String json = createJson(Command.UNSUBSCRIBE, event, this.nonce++);
        final Frame unsubscribeFrame = this.createFrame(json);
        this.queueOut.add(unsubscribeFrame);
    }

    private static String createJson(Command command, Event event, int nonce) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
        try (baos) {
            writer.beginObject();
            writer.name("cmd").value(command.name());
            writer.name("evt").value(event.name());
            writer.name("nonce").value(Integer.toString(nonce));
            writer.endObject();
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private Frame createFrame(String json) {
        final byte[] payloadBuf = new byte[FRAME_LENGTH - 8]; // subtract header
        final byte[] source = json.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(source, 0, payloadBuf, 0, source.length);
        return new Frame(Opcode.FRAME, source.length, payloadBuf);
    }

    public void abort() {
        this.aborted = true;
    }

    @Override
    public void onConnect(Message message) {
        final DiscordUser user = this.gson.fromJson(message.getData().get("user"), DiscordUser.class);
        this.connectedUser = user;
        this.wasConnectedRecently.set(true);
        System.out.println(user);
    }

    @Override
    public void onDisconnect() {
        this.wasDisconnectedRecently.set(true);
    }

}
