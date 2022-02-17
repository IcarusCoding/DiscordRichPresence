package de.intelligence.drp.core.dc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import org.json.JSONObject;

import de.intelligence.drp.api.IDiscord;
import de.intelligence.drp.api.RichPresence;
import de.intelligence.drp.api.Updatable;
import de.intelligence.drp.api.event.CloseEvent;
import de.intelligence.drp.api.event.DiscordEvent;
import de.intelligence.drp.api.event.ReadyEvent;
import de.intelligence.drp.api.user.IDiscordUser;
import de.intelligence.drp.core.connection.IIPCConnection;
import de.intelligence.drp.core.connection.IPCConnectionImpl;
import de.intelligence.drp.core.connection.IRPCConnection;
import de.intelligence.drp.core.connection.IRPCEventHandler;
import de.intelligence.drp.core.dc.connection.DiscordRPCConnection;
import de.intelligence.drp.core.dc.event.DiscordEventEmitter;
import de.intelligence.drp.core.dc.json.Message;
import de.intelligence.drp.core.dc.json.MessageDeserializer;
import de.intelligence.drp.core.dc.pojo.ConnectionInfo;
import de.intelligence.drp.core.dc.pojo.DiscordUserImpl;
import de.intelligence.drp.core.dc.proto.Command;
import de.intelligence.drp.core.dc.proto.EventTypes;
import de.intelligence.drp.core.dc.proto.Frame;
import de.intelligence.drp.core.dc.proto.Opcode;
import de.intelligence.drp.core.event.IEventEmitter;
import de.intelligence.drp.core.os.OSUtils;

final class DiscordWrapper implements IDiscord, IRPCEventHandler<Message> {

    private final String applicationId;
    private final IEventEmitter<DiscordEvent> eventEmitter;
    private final int pid;
    private final Gson gson;
    private final IRPCConnection<Message> rpcConnection;
    private final Supplier<Boolean> retryFunc;
    private final ReentrantLock reentrantLock;
    private final Condition condition;
    private final Queue<Frame> queueOut;

    private RichPresence currentPresence;
    private boolean initialized;
    private boolean abort;
    private IDiscordUser connectedDiscordUser;
    private int nonce;

    public DiscordWrapper(String applicationId) {
        this.applicationId = applicationId;
        this.eventEmitter = new DiscordEventEmitter();
        this.pid = OSUtils.getOsDependent().getCurrentPID();
        this.gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageDeserializer()).create();
        final IIPCConnection ipcConn = new IPCConnectionImpl();
        ipcConn.setNamedPipes(Arrays.asList("\\\\.\\pipe\\discord-ipc-0", "\\\\.\\pipe\\discord-ipc-1", "\\\\.\\pipe\\discord-ipc-2"));
        this.rpcConnection = new DiscordRPCConnection(new ConnectionInfo(1, this.applicationId), this.gson, ipcConn);
        this.retryFunc = Retry.decorateSupplier(Retry.of("initConn", RetryConfig.<Boolean>custom()
                .maxAttempts(DiscordConsts.MAX_RETRY_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(DiscordConsts.EXPONENTIAL_BACKOFF_INIT,
                        IntervalFunction.DEFAULT_MULTIPLIER))
                .retryOnResult(a -> a).build()), () -> {
            this.rpcConnection.connect();
            return !this.rpcConnection.isConnected();
        });
        this.reentrantLock = new ReentrantLock();
        this.condition = this.reentrantLock.newCondition();
        this.queueOut = new LinkedBlockingQueue<>();
    }

    @Override
    public void registerEventHandler(Object obj) {
        this.eventEmitter.register(obj);
    }

    @Override
    public void unregisterEventHandler(Object obj) {
        this.eventEmitter.unregister(obj);
    }

    @Override
    public void setRichPresence(RichPresence presence) {
        Objects.requireNonNull(presence);
        if (this.currentPresence != null && !this.currentPresence.equals(presence)) {
            this.currentPresence.removeObserver(this);
        }
        if (!presence.equals(this.currentPresence)) {
            this.currentPresence = presence;
            this.currentPresence.addObserver(this);
        }
        final JSONObject rootJson = new JSONObject()
                .put("cmd", Command.SET_ACTIVITY.name())
                .put("nonce", Integer.toString(this.nonce++))
                .put("args", new JSONObject()
                        .put("pid", this.pid)
                        .put("activity", presence.convertToJson()));
        System.out.println("WANTED ACTIVITY: " + rootJson.toString());
        final Frame activityUpdateFrame = this.createFrame(rootJson.toString());
        this.queueOut.add(activityUpdateFrame);
    }

    @Override
    public void connect() {
        if (this.initialized) {
            return;
        }
        this.rpcConnection.addEventHandler(this);
        this.initialized = true;
        this.update();
        while (!this.abort) {
            this.reentrantLock.lock();
            try {
                this.condition.await(DiscordConsts.MAX_ACTIVITY_WAIT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }
            this.update();
        }
        this.rpcConnection.disconnect();
        this.currentPresence = null;
        this.queueOut.clear();
        this.abort = false;
        this.nonce = 0;
        this.connectedDiscordUser = null;
        this.initialized = false;
    }

    @Override
    public void disconnect() {
        this.abort = true;
    }

    private void update() {
        System.out.println("UPDATE CONN");
        if (!this.rpcConnection.isConnected()) {
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
                if (message.getEvent() == EventTypes.ERROR) {
                    System.out.println("ERROR");
                }
            } else {
                System.out.println("NO NONCE <---------- TODO");
            }
        }
        while (!this.queueOut.isEmpty()) {
            final Frame frameOut = this.queueOut.poll();
            this.rpcConnection.send(frameOut.toByteArray(), frameOut.getFullLength());
        }
    }

    private Frame createFrame(String json) {
        final byte[] payloadBuf = new byte[DiscordConsts.FRAME_LENGTH - 8]; // subtract header
        final byte[] source = json.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(source, 0, payloadBuf, 0, source.length);
        return new Frame(Opcode.FRAME, source.length, payloadBuf);
    }

    @Override
    public void onConnect(Message response) {
        final IDiscordUser discordUser = this.gson.fromJson(response.getData().get("user"), DiscordUserImpl.class);
        this.connectedDiscordUser = discordUser;
        this.eventEmitter.emit(new ReadyEvent(this, discordUser));
    }

    @Override
    public void onDisconnect() {
        this.eventEmitter.emit(new CloseEvent());
    }

    @Override
    public void notifyUpdate(Updatable source) {
        if (this.currentPresence.equals(source)) {
            this.setRichPresence((RichPresence) source);
        }
    }

}
