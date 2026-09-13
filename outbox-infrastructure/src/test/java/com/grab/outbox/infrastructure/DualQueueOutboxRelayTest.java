package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxWorkSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DualQueueOutboxRelayTest {

    private DualQueueOutboxRelay<Long> relay;

    @AfterEach
    void stopRelay() {
        if (relay != null) {
            relay.stop();
        }
    }

    @Test
    void enqueueHot_dropsWhenFull() {
        relay = new DualQueueOutboxRelay<>("test", 1, 1, 1, Duration.ofSeconds(1));

        assertTrue(relay.enqueueHot(1L));
        assertFalse(relay.enqueueHot(2L));
    }

    @Test
    void worker_prefersHotQueue() throws Exception {
        relay = new DualQueueOutboxRelay<>("test", 1, 8, 8, Duration.ofSeconds(1));
        List<OutboxWorkSource> sources = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);
        relay.setHandler(work -> {
            sources.add(work.source());
            latch.countDown();
        });
        assertTrue(relay.enqueueHot(1L));
        assertTrue(relay.enqueueHot(2L));
        assertTrue(relay.enqueueCold(new ClaimedOutboxEvent<>(3L, "token")));
        relay.start();

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(List.of(OutboxWorkSource.HOT, OutboxWorkSource.HOT, OutboxWorkSource.COLD), firstThree(sources));
    }

    @Test
    void inflight_dropsDuplicateIdWhileProcessing() throws Exception {
        relay = new DualQueueOutboxRelay<>("test", 2, 8, 8, Duration.ofSeconds(1));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Long> handled = new CopyOnWriteArrayList<>();
        relay.setHandler(work -> {
            handled.add(work.id());
            started.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(relay.enqueueHot(9L));
        assertTrue(relay.enqueueHot(9L));
        relay.start();
        assertTrue(started.await(2, TimeUnit.SECONDS));
        Thread.sleep(200);
        release.countDown();
        Thread.sleep(200);
        assertEquals(1, handled.size());
    }

    private static List<OutboxWorkSource> firstThree(List<OutboxWorkSource> sources) {
        return new ArrayList<>(sources.subList(0, Math.min(3, sources.size())));
    }
}
