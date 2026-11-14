package com.grab.outbox.infrastructure.claim;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.outbox.infrastructure.OutboxClaimRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * How competing workers claim outbox rows without dispatching the same row twice. The strategy is chosen per
 * module by configuration ({@code <module>.outbox.claim-strategy}, see {@link OutboxClaimStrategies}) to match
 * the module's database; callers ({@code AbstractOutboxProcessor}) never see which one is in use.
 *
 * <p>All methods run inside the caller's (short) claim or outcome transaction. Every claim writes a claim token;
 * outcomes are fenced on that token, so a worker whose claim was reclaimed after {@code claimTimeout} cannot
 * overwrite the new owner's state.</p>
 */
public interface OutboxClaimStrategy {

    /**
     * Configuration value of this strategy, e.g. {@code skip-locked}.
     */
    String name();

    <T extends OutboxEntry<ID>, ID> List<ClaimedOutboxEvent<ID>> claimBatch(
            OutboxClaimContext<T> context,
            OutboxClaimRequest request
    );

    <T extends OutboxEntry<ID>, ID> Optional<ClaimedOutboxEvent<ID>> claimOne(
            OutboxClaimContext<T> context,
            ID id,
            Instant now
    );

    /**
     * Re-reads a row under a blocking row lock ({@code SELECT … FOR UPDATE}) so an outcome is applied only if the
     * caller still holds the claim. Blocking row locks exist on every supported database, so strategies normally
     * keep this default.
     */
    default <T extends OutboxEntry<ID>, ID> Optional<T> lockForOutcome(OutboxClaimContext<T> context, ID id) {
        EntityManager entityManager = context.entityManager();
        T managed = entityManager.find(context.entityType(), id);
        if (managed == null) {
            return Optional.empty();
        }
        // refresh (not find) so a row already in the persistence context is re-read under the lock
        entityManager.refresh(managed, LockModeType.PESSIMISTIC_WRITE);
        return Optional.of(managed);
    }
}
