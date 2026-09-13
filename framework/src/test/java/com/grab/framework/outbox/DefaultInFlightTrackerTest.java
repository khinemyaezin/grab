package com.grab.framework.outbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultInFlightTrackerTest {

    private final DefaultInFlightTracker<Long> tracker = new DefaultInFlightTracker<>();

    @Test
    void tryAcquire_rejectsDuplicateUntilReleased() {
        assertTrue(tracker.tryAcquire(1L));
        assertFalse(tracker.tryAcquire(1L));

        tracker.release(1L);

        assertTrue(tracker.tryAcquire(1L));
    }
}
