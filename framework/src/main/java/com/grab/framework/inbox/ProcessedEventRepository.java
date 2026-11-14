package com.grab.framework.inbox;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumer-side inbox: records that a consumer has applied an outbox event so redeliveries become no-ops.
 * <p>
 * {@link #markProcessed} MUST be called inside the same transaction as the consumer's side effect, before
 * that side effect. A {@code false} result means the event was already applied and the caller returns early.
 */
public interface ProcessedEventRepository {

    /**
     * @return {@code true} when this is the first time {@code consumer} sees {@code eventId};
     * {@code false} when it was already recorded (duplicate delivery).
     */
    boolean markProcessed(String consumer, UUID eventId);

    /**
     * Deletes up to {@code limit} rows recorded before {@code cutoff}. Requires an active transaction.
     *
     * @return the number of rows deleted
     */
    int deleteProcessedBefore(Instant cutoff, int limit);
}
