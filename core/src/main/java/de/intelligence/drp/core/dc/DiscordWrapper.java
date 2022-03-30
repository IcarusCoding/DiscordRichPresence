package de.intelligence.drp.core.dc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
import de.intelligence.drp.api.event.ActivityJoinEvent;
import de.intelligence.drp.api.event.CloseEvent;
import de.intelligence.drp.api.event.DiscordEvent;
import de.intelligence.drp.api.event.ErrorEvent;
import de.intelligence.drp.api.event.EventType;
import de.intelligence.drp.api.event.ReadyEvent;
import de.intelligence.drp.api.exception.ErrorCode;
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
import de.intelligence.drp.core.dc.proto.Frame;
import de.intelligence.drp.core.dc.proto.InternalEventType;
import de.intelligence.drp.core.dc.proto.Opcode;
import de.intelligence.drp.core.event.IEventEmitter;
import de.intelligence.drp.core.exception.ConnectionException;
import de.intelligence.drp.core.exception.InvalidMessageException;
import de.intelligence.drp.core.exception.ReadFailureException;
import de.intelligence.drp.core.exception.WriteFailureException;
import de.intelligence.drp.core.os.OSUtils;

public final class DiscordWrapper implements IDiscord, IRPCEventHandler<Message> {

    private final String applicationId;
    private final IEventEmitter<DiscordEvent> eventEmitter;
    private final int pid;
    private final Gson gson;
    private final AtomicReference<Exception> lastException;
    private final IRPCConnection<Message> rpcConnection;
    private final Supplier<Boolean> retryFunc;
    private final ReentrantLock reentrantLock;
    private final Condition condition;
    private final Queue<Frame> queueOut;
    private final EnumSet<InternalEventType> subscriptions;
    private final ScheduledExecutorService schedulerOut;

    private RichPresence currentPresence;
    private boolean initialized;
    private boolean abort;
    private IDiscordUser connectedDiscordUser;
    private int nonce;

    DiscordWrapper(String applicationId) {
        this.applicationId = applicationId;
        this.eventEmitter = new DiscordEventEmitter(this);
        this.pid = OSUtils.getOsDependent().getCurrentPID();
        this.gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageDeserializer()).create();
        this.lastException = new AtomicReference<>();
        final IIPCConnection ipcConn = new IPCConnectionImpl();
        ipcConn.setNamedPipes(Arrays.asList("discord-ipc-0", "discord-ipc-1", "discord-ipc-2"));
        this.rpcConnection = new DiscordRPCConnection(new ConnectionInfo(1, this.applicationId), this.gson, ipcConn);
        this.retryFunc = Retry.decorateSupplier(Retry.of("initConn", RetryConfig.<Boolean>custom()
                .maxAttempts(DiscordConsts.MAX_RETRY_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(DiscordConsts.EXPONENTIAL_BACKOFF_INIT,
                        IntervalFunction.DEFAULT_MULTIPLIER))
                .retryOnResult(a -> a).build()), () -> {
            try {
                this.rpcConnection.connect();
                this.lastException.set(null);
            } catch (ConnectionException ex) {
                this.lastException.set(ex);
            }
            return !this.rpcConnection.isConnected();
        });
        this.reentrantLock = new ReentrantLock();
        this.condition = this.reentrantLock.newCondition();
        this.queueOut = new LinkedBlockingQueue<>();
        this.subscriptions = EnumSet.noneOf(InternalEventType.class);
        this.schedulerOut = Executors.newScheduledThreadPool(1, r -> {
            final Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
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
                .put("nonce", Integer.toString(this.nonce))
                .put("args", new JSONObject()
                        .put("pid", this.pid)
                        .put("activity", presence.convertToJson()));
        this.nonce++;
        final Frame activityUpdateFrame = DiscordWrapper.createFrame(rootJson.toString().getBytes(StandardCharsets.UTF_8));
        this.queueOut.add(activityUpdateFrame);
    }

    @Override
    public void connect() {
        this.connect(false);
    }

    @Override
    public void connectAsync() {
        this.connect(true);
    }

    private void connect(boolean startNewThread) {
        if (this.initialized) {
            return;
        }
        this.rpcConnection.addEventHandler(this);
        if (!startNewThread) {
            this.connect0();
        } else {
            final Thread t = new Thread(this::connect0);
            t.setDaemon(true);
            t.start();
        }
    }

    @Override
    public void disconnect() {
        this.abort = true;
    }

    @Override
    public Optional<IDiscordUser> getConnectedUser() {
        return Optional.ofNullable(this.connectedDiscordUser);
    }

    @Override
    public String getApplicationId() {
        return this.applicationId;
    }

    @Override
    public void subscribe(EventType type) {
        if (type != EventType.NONE) {
            final InternalEventType convType = InternalEventType.fromEventType(type);
            if (convType != InternalEventType.NONE && !this.subscriptions.contains(convType)) {
                this.subscriptions.add(convType);
                this.subscribe0(convType);
            }
        }
    }

    @Override
    public void unsubscribe(EventType type) {
        if (type != EventType.NONE) {
            final InternalEventType convType = InternalEventType.fromEventType(type);
            if (convType != InternalEventType.NONE && this.subscriptions.contains(convType)) {
                this.subscriptions.remove(convType);
                this.unsubscribe0(convType);
            }
        }
    }

    private void connect0() {
        this.initialized = true;
        try {
            this.update();
            this.terminateIfError();
            this.schedulerOut.scheduleAtFixedRate(() -> {
                if (this.rpcConnection.isConnected() && !this.queueOut.isEmpty()) {
                    final Frame frameOut = this.queueOut.poll();
                    try {
                        this.rpcConnection.send(frameOut.toByteArray(), frameOut.getFullLength());
                    } catch (WriteFailureException ex) {
                        this.lastException.set(ex);
                    }
                }
            }, 0, DiscordConsts.OUTBOUND_DELAY, TimeUnit.MILLISECONDS);
            while (!this.abort) {
                this.reentrantLock.lock();
                if (!this.await()) {
                    this.terminateIfError();
                    return;
                }
                this.update();
            }
        } catch (ReadFailureException | InvalidMessageException ex) {
            this.lastException.set(ex);
            this.terminateIfError();
        }
        this.schedulerOut.shutdownNow();
        this.rpcConnection.disconnect();
    }

    private boolean await() {
        try {
            this.condition.await(DiscordConsts.MAX_ACTIVITY_WAIT, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException ex) {
            this.lastException.set(ex);
        }
        return false;
    }

    private void subscribe0(InternalEventType type) {
        this.queueOut.add(DiscordWrapper.createFrame(DiscordWrapper.createSimpleJson(Command.SUBSCRIBE, type, this.nonce)));
        this.nonce++;
    }

    private void unsubscribe0(InternalEventType type) {
        this.queueOut.add(DiscordWrapper.createFrame(DiscordWrapper.createSimpleJson(Command.UNSUBSCRIBE, type, this.nonce)));
        this.nonce++;
    }

    private void update() throws ReadFailureException, InvalidMessageException {
        if (!this.rpcConnection.isConnected()) {
            this.retryFunc.get();
            return;
        }
        while (true) {
            final byte[] readBuf = this.rpcConnection.receive(0);
            if (readBuf.length == 0) {
                break;
            }
            final Message message = this.gson.fromJson(new String(readBuf, StandardCharsets.UTF_8), Message.class);
            if (message.hasNonce()) {
                if (message.getEvent() == InternalEventType.ERROR) {
                    throw new InvalidMessageException(message.getData().get("message").getAsString(), ErrorCode.UNSPECIFIED);
                }
            } else if (message.getEvent() == InternalEventType.ACTIVITY_JOIN && message.getData().has("secret")) {
                this.eventEmitter.emit(new ActivityJoinEvent(this, message.getData().get("secret").getAsString()));
            }
        }
    }

    private void terminateIfError() {
        final Exception lastEx = this.lastException.get();
        if (lastEx != null) {
            this.eventEmitter.emit(new ErrorEvent(lastEx, lastEx instanceof ConnectionException ex ? ex.getErrorCode() : ErrorCode.UNSPECIFIED));
            this.disconnect();
        }
    }

    private static byte[] createSimpleJson(Command command, InternalEventType eventType, int nonce) {
        return new JSONObject()
                .put("cmd", command.name())
                .put("evt", eventType.name())
                .put("nonce", Integer.toString(nonce))
                .toString().getBytes(StandardCharsets.UTF_8);
    }

    private static Frame createFrame(byte[] json) {
        final byte[] payloadBuf = new byte[DiscordConsts.FRAME_LENGTH - DiscordConsts.HEADER_SIZE];
        System.arraycopy(json, 0, payloadBuf, 0, json.length);
        return new Frame(Opcode.FRAME, json.length, payloadBuf);
    }

    @Override
    public void onConnect(Message response) {
        final IDiscordUser discordUser = this.gson.fromJson(response.getData().get("user"), DiscordUserImpl.class);
        this.connectedDiscordUser = discordUser;
        this.eventEmitter.emit(new ReadyEvent(this, discordUser));
    }

    @Override
    public void onDisconnect() {
        this.rpcConnection.removeEventHandler(this);
        this.currentPresence = null;
        this.queueOut.clear();
        this.abort = false;
        this.nonce = 0;
        this.connectedDiscordUser = null;
        this.lastException.set(null);
        this.initialized = false;
        this.eventEmitter.emit(new CloseEvent());
    }

    @Override
    public void notifyUpdate(Updatable source) {
        if (this.currentPresence.equals(source)) {
            this.setRichPresence((RichPresence) source);
        }
    }

}
