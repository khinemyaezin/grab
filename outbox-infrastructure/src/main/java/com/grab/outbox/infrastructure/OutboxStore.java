package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OutboxStore<T extends OutboxEntry<ID>, ID> {

    void saveAll(Collection<T> events);

    /**
     * Locks claimable rows with {@code FOR UPDATE SKIP LOCKED}: rows locked by another replica are skipped,
     * so competing pollers partition the backlog instead of queueing behind each other.
     */
    List<T> findBatchForProcessing(
            Collection<OutboxStatus> retryableStatuses,
            OutboxStatus processingStatus,
            Instant availableAt,
            Instant staleBefore,
            int batchSize
    );

    Optional<T> findById(ID id);

    /**
     * Non-blocking claim used by the hot path. Empty when the row is missing or locked by another worker.
     */
    default Optional<T> findByIdForUpdateSkipLocked(ID id) {
        return findById(id);
    }

    /**
     * Re-reads the row's current state under a blocking row lock. Used for status transitions after dispatch
     * so a claim lost to another replica is detected instead of overwritten.
     */
    default Optional<T> findByIdForUpdate(ID id) {
        return findById(id);
    }

    default void flush() {
    }

    /**
     * Deletes at most {@code limit} published rows older than {@code cutoff}.
     *
     * @return the number of deleted rows
     */
    int deletePublishedOlderThan(Instant cutoff, int limit);
}
