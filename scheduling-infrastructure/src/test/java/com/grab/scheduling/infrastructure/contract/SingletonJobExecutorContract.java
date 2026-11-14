package com.grab.scheduling.infrastructure.contract;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SingletonJobExecutorContract {

    protected abstract SingletonJobExecutor owner();

    protected abstract SingletonJobExecutor competitor();

    protected abstract void awaitLease(Duration duration) throws InterruptedException;

    @Test
    void excludesConcurrentTasksForTheSameJobAndReleasesOnCompletion() throws Exception {
        SingletonJob job = job(Duration.ofSeconds(30), Duration.ZERO);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        try (var workers = Executors.newSingleThreadExecutor()) {
            var result = workers.submit(() -> owner().execute(job, () -> {
                started.countDown();
                await(finish);
            }));
            try {
                assertTrue(started.await(10, TimeUnit.SECONDS));
                assertFalse(competitor().execute(job, () -> fail("Lock held by owner")));
            } finally {
                finish.countDown();
            }
            assertTrue(result.get(10, TimeUnit.SECONDS));
        }
        assertTrue(competitor().execute(job, () -> { }));
    }

    @Test
    void releasesOnFailure() {
        SingletonJob job = job(Duration.ofSeconds(30), Duration.ZERO);
        assertThrows(IllegalStateException.class, () -> owner().execute(job, () -> {
            throw new IllegalStateException("fixture");
        }));
        assertTrue(competitor().execute(job, () -> { }));
    }

    @Test
    void respectsLockAtLeastForAfterCompletion() throws InterruptedException {
        Duration hold = Duration.ofSeconds(2);
        SingletonJob job = job(Duration.ofSeconds(30), hold);
        assertTrue(owner().execute(job, () -> { }));
        assertFalse(competitor().execute(job, () -> fail("Minimum lease still active")));
        awaitLease(hold);
        assertTrue(competitor().execute(job, () -> { }));
    }

    @Test
    void lockAtMostForAllowsTakeoverWhileOriginalTaskIsStillRunning() throws Exception {
        Duration lease = Duration.ofSeconds(2);
        SingletonJob job = job(lease, lease);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        try (var workers = Executors.newSingleThreadExecutor()) {
            var result = workers.submit(() -> owner().execute(job, () -> {
                started.countDown();
                await(finish);
            }));
            try {
                assertTrue(started.await(10, TimeUnit.SECONDS));
                awaitLease(lease);
                assertTrue(competitor().execute(job, () -> { }));
            } finally {
                finish.countDown();
            }
            assertTrue(result.get(10, TimeUnit.SECONDS));
        }
    }

    private static SingletonJob job(Duration atMost, Duration atLeast) {
        return new SingletonJob("contract." + UUID.randomUUID(), atMost, atLeast);
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            fail(exception);
        }
    }
}
