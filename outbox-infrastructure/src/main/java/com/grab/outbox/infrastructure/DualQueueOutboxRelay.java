package com.grab.outbox.infrastructure;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.DefaultInFlightTracker;
import com.grab.framework.outbox.InFlightTracker;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.outbox.OutboxWorkHandler;
import com.grab.framework.outbox.QueuedOutboxWork;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class DualQueueOutboxRelay<ID> implements OutboxRelay<ID> {

    private static final Logger log = Loggers.getLogger(DualQueueOutboxRelay.class);
    private static final long IDLE_POLL_MS = 100L;

    private final String name;
    private final int workerCount;
    private final Duration drainTimeout;
    private final BlockingQueue<QueuedOutboxWork<ID>> hotQueue;
    private final BlockingQueue<QueuedOutboxWork<ID>> coldQueue;
    private final InFlightTracker<ID> inFlight;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicInteger pollTick = new AtomicInteger();
    private final List<Thread> workers = new ArrayList<>();

    private volatile OutboxWorkHandler<ID> handler;

    public DualQueueOutboxRelay(
            String name,
            int workerCount,
            int hotQueueCapacity,
            int coldQueueCapacity,
            Duration drainTimeout
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.workerCount = Math.max(1, workerCount);
        this.drainTimeout = drainTimeout == null ? Duration.ofSeconds(5) : drainTimeout;
        this.hotQueue = new ArrayBlockingQueue<>(Math.max(1, hotQueueCapacity));
        this.coldQueue = new ArrayBlockingQueue<>(Math.max(1, coldQueueCapacity));
        this.inFlight = new DefaultInFlightTracker<>();
    }

    @Override
    public boolean enqueueHot(ID id) {
        if (id == null || stopped.get()) {
            return false;
        }
        boolean offered = hotQueue.offer(QueuedOutboxWork.hot(id));
        if (!offered) {
            log.warn("Hot queue full for {} outbox; dropping id={}. Poller will recover", name, id);
        }
        return offered;
    }

    @Override
    public boolean enqueueCold(ClaimedOutboxEvent<ID> claimed) {
        if (claimed == null || claimed.id() == null || stopped.get()) {
            return false;
        }
        return coldQueue.offer(QueuedOutboxWork.cold(claimed));
    }

    @Override
    public int coldQueueRemainingCapacity() {
        return coldQueue.remainingCapacity();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void setHandler(OutboxWorkHandler<ID> handler) {
        this.handler = handler;
    }

    @Override
    public synchronized void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        stopped.set(false);
        workers.clear();
        for (int index = 1; index <= workerCount; index++) {
            Thread worker = Thread.ofPlatform()
                    .name(name + "-outbox-worker-" + index)
                    .daemon(true)
                    .unstarted(this::runWorker);
            workers.add(worker);
            worker.start();
        }
    }

    @Override
    public synchronized void stop() {
        stopped.set(true);
        if (!running.compareAndSet(true, false)) {
            return;
        }
        for (Thread worker : workers) {
            worker.interrupt();
        }
        long deadlineNanos = System.nanoTime() + drainTimeout.toNanos();
        for (Thread worker : workers) {
            long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
            if (remainingMs <= 0) {
                break;
            }
            try {
                worker.join(remainingMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        workers.clear();
    }

    private void runWorker() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                QueuedOutboxWork<ID> work = nextWork();
                if (work == null) {
                    continue;
                }
                if (!inFlight.tryAcquire(work.id())) {
                    continue;
                }
                try {
                    OutboxWorkHandler<ID> current = handler;
                    if (current != null) {
                        current.handle(work);
                    }
                } catch (RuntimeException exception) {
                    log.warn("Outbox worker failed for {} id={}", name, work.id(), exception);
                } finally {
                    inFlight.release(work.id());
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private QueuedOutboxWork<ID> nextWork() throws InterruptedException {
        int turn = Math.floorMod(pollTick.getAndIncrement(), 3);
        if (turn < 2) {
            QueuedOutboxWork<ID> hot = hotQueue.poll();
            if (hot != null) {
                return hot;
            }
            QueuedOutboxWork<ID> cold = coldQueue.poll();
            if (cold != null) {
                return cold;
            }
            return hotQueue.poll(IDLE_POLL_MS, TimeUnit.MILLISECONDS);
        }
        QueuedOutboxWork<ID> cold = coldQueue.poll();
        if (cold != null) {
            return cold;
        }
        QueuedOutboxWork<ID> hot = hotQueue.poll();
        if (hot != null) {
            return hot;
        }
        return coldQueue.poll(IDLE_POLL_MS, TimeUnit.MILLISECONDS);
    }
}
