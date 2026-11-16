package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ModuleJobSchedulerTest {

    @Test
    void stopWaitsForAnActivePollAndCancelsPendingRuns() throws Exception {
        CountDownLatch pollStarted = new CountDownLatch(1);
        CountDownLatch finishPoll = new CountDownLatch(1);
        CountDownLatch stopping = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        AtomicInteger polls = new AtomicInteger();
        CompetingWorkerTask poll = new CompetingWorkerTask("catalog.outbox.poll", "claim token",
                Duration.ZERO, Duration.ofMillis(10), () -> {
                    polls.incrementAndGet();
                    pollStarted.countDown();
                    await(finishPoll);
                });
        ModuleJobScheduler scheduler = new ModuleJobScheduler("catalog", List.of(poll), 1);
        scheduler.start();

        try (var workers = Executors.newSingleThreadExecutor()) {
            try {
                assertTrue(pollStarted.await(5, TimeUnit.SECONDS));
                workers.submit(() -> {
                    stopping.countDown();
                    scheduler.stop();
                    stopped.countDown();
                });
                assertTrue(stopping.await(5, TimeUnit.SECONDS));
                assertEquals(1, stopped.getCount());
            } finally {
                finishPoll.countDown();
                scheduler.stop();
            }
            assertTrue(stopped.await(5, TimeUnit.SECONDS));
        }
        assertEquals(1, polls.get());
    }

    @Test
    void cleanupUsesTheSingletonExecutorOnce() throws InterruptedException {
        AtomicInteger invocations = new AtomicInteger();
        AtomicInteger bodyRuns = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(1);
        SingletonJob job = new SingletonJob("catalog.outbox.cleanup", Duration.ofMinutes(10), Duration.ZERO);
        SingletonJobExecutor heldElsewhere = (candidate, body) -> {
            assertEquals(job, candidate);
            invocations.incrementAndGet();
            done.countDown();
            return false;
        };
        ClusterSingletonTask cleanup = new ClusterSingletonTask(job, heldElsewhere, Duration.ZERO,
                Duration.ofHours(1), bodyRuns::incrementAndGet);
        ModuleJobScheduler scheduler = new ModuleJobScheduler("catalog", List.of(cleanup), 1);
        scheduler.start();
        try {
            assertTrue(done.await(5, TimeUnit.SECONDS));
        } finally {
            scheduler.stop();
        }
        assertEquals(1, invocations.get());
        assertEquals(0, bodyRuns.get());
    }

    @Test
    void rejectsDuplicateNames() {
        CompetingWorkerTask poll = new CompetingWorkerTask("catalog.outbox.poll", "claim token",
                Duration.ZERO, Duration.ofSeconds(1), () -> { });
        assertThrows(IllegalArgumentException.class, () -> new ModuleJobScheduler("catalog", List.of(poll, poll), 1));
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            fail(exception);
        }
    }
}
