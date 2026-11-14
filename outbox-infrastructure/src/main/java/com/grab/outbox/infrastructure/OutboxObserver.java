package com.grab.outbox.infrastructure;

import java.time.Duration;

/**
 * Hook for outbox delivery metrics. Implemented with Micrometer in the app; no-op by default.
 */
public interface OutboxObserver {

    OutboxObserver NOOP = new OutboxObserver() {
    };

    static OutboxObserver noop() {
        return NOOP;
    }

    default void published(String module, String eventType, Duration deliveryLag) {
    }

    default void failed(String module, String eventType, int attempt) {
    }

    default void dead(String module, String eventType) {
    }

    /**
     * A status transition found the row no longer claimed by this worker (reclaimed after the claim timeout).
     */
    default void claimLost(String module) {
    }

    default void claimsReleased(String module, int count) {
    }

    default void cleanedUp(String module, int deleted) {
    }
}
