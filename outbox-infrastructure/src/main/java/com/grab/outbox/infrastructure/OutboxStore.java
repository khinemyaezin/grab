package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OutboxStore<T extends OutboxEntry<ID>, ID> {

    void saveAll(Collection<T> events);

    /**
     * Claims up to {@code request.limit()} claimable rows (marks them {@code PROCESSING} with a claim token) and
     * returns only the rows this caller now owns. How competing workers are kept apart is the store's
     * {@code OutboxClaimStrategy}: {@code SKIP LOCKED} row locks, a conditional update, etc.
     */
    List<ClaimedOutboxEvent<ID>> claimBatch(OutboxClaimRequest request);

    Optional<T> findById(ID id);

    /**
     * Hot-path claim of a single row. Empty when the row is missing, not yet available, not in
     * {@code NEW}/{@code FAILED}, or already claimed by another worker. Never blocks on another worker's claim
     * for longer than that worker's claim transaction.
     */
    Optional<ClaimedOutboxEvent<ID>> claimOne(ID id, Instant now);

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

    /**
     * Rows not yet published or dead-lettered, with the oldest {@code occurredAt} among them.
     */
    default OutboxBacklog backlog(Collection<OutboxStatus> pendingStatuses) {
        return OutboxBacklog.EMPTY;
    }

    /**
     * Locks up to {@code limit} {@code DEAD} rows with {@code fromId <= id <= toId}, ordered by id.
     */
    default List<T> findDeadForReplay(ID fromId, ID toId, int limit) {
        return List.of();
    }
}
