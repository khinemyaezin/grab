package com.grab.framework.outbox;

import java.time.Instant;
import java.util.UUID;

public interface OutboxEntry<ID> {
    ID getId();

    default String getAggregateType() {
        return null;
    }

    default String getAggregateId() {
        return null;
    }

    UUID getEventId();

    String getEventType();

    String getPayload();

    int getEventVersion();

    String getHeaders();

    OutboxStatus getStatus();

    String getClaimToken();

    int getAttemptCount();

    Instant getOccurredAt();

    Instant getAvailableAt();

    void markProcessing(Instant now, String claimToken);

    void markPublished(Instant now);

    void markFailed(String error, Instant nextAvailableAt);

    void markDead(Instant now, String error);

    void releaseClaim(Instant now);

    /**
     * Operator replay of a dead-lettered row: back to {@code NEW} with a fresh attempt budget.
     */
    default void requeue(Instant now) {
        throw new UnsupportedOperationException("requeue is not supported by " + getClass().getName());
    }

    default boolean isClaimedBy(String claimToken) {
        return getStatus() == OutboxStatus.PROCESSING
                && claimToken != null
                && claimToken.equals(getClaimToken());
    }
}
