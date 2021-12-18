package de.intelligence.drp.dc;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import de.intelligence.drp.Initializable;
import de.intelligence.drp.Test;
import de.intelligence.drp.connection.IIPCConnection;
import de.intelligence.drp.connection.IPCConnectionImpl;
import de.intelligence.drp.connection.IRPCConnection;
import de.intelligence.drp.dc.connection.DiscordRPCConnection;
import de.intelligence.drp.dc.pojo.ConnectionInfo;

public final class DiscordWrapper implements Initializable {

    private static final int MAX_ACTIVITY_WAIT = 1000;

    private final String applicationId;
    private final ReentrantLock reentrantLock;
    private final Condition activityCondition;
    private final IRPCConnection rpcConnection;
    private final Retry backoff;
    private final Supplier<Boolean> retryFunc;

    private boolean initialized;
    private boolean aborted;

    public DiscordWrapper(String applicationId) {
        this.applicationId = applicationId;
        this.reentrantLock = new ReentrantLock();
        this.activityCondition = this.reentrantLock.newCondition();
        final IIPCConnection conn = new IPCConnectionImpl();
        conn.setNamedPipes(Collections.singletonList("\\\\.\\pipe\\discord-ipc-0"));
        this.rpcConnection = new DiscordRPCConnection(new ConnectionInfo(1, this.applicationId), conn);
        this.backoff = Retry.of("initConn", RetryConfig.<Boolean>custom().maxAttempts(5)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(500, IntervalFunction.DEFAULT_MULTIPLIER))
                .retryOnResult(a -> a).build());
        this.retryFunc = Retry.decorateSupplier(this.backoff, () -> {
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

    private void updateConnection() {
        if (!this.rpcConnection.isConnected()) {
            System.out.println("INIT CONN");
            this.retryFunc.get();
            return;
        }

    }

    public void abort() {
        this.aborted = true;
    }

}
