package com.grab.outbox.infrastructure.adapter.out.jpa.claim;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.core.port.out.OutboxClaimRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Pessimistic claim that skips rows locked by other workers: {@code PESSIMISTIC_WRITE} plus the lock-timeout hint
 * {@code -2}, which Hibernate renders per dialect as {@code FOR UPDATE SKIP LOCKED} (PostgreSQL, MySQL ≥ 8.0,
 * MariaDB ≥ 10.6, Oracle, DB2) or {@code WITH (UPDLOCK, ROWLOCK, READPAST)} (SQL Server). Competing workers
 * partition the backlog instead of queueing behind each other.
 *
 * <p>Do not configure it for databases without skip-locked support (H2, MySQL 5.7): Hibernate silently drops the
 * hint there, so claims stay correct but workers serialise. Use {@code conditional-update} instead.</p>
 */
public final class SkipLockedClaimStrategy implements OutboxClaimStrategy {

    public static final String NAME = "skip-locked";

    static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";
    /**
     * Hibernate's {@code LockOptions.SKIP_LOCKED}.
     */
    static final int SKIP_LOCKED = -2;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public <T extends OutboxEntry<ID>, ID> List<ClaimedOutboxEvent<ID>> claimBatch(
            OutboxClaimContext<T> context,
            OutboxClaimRequest request
    ) {
        String jpql = """
                select event
                from %s event
                where %s
                order by event.id asc
                """.formatted(context.entityName(), OutboxClaimJpql.BATCH_CLAIMABLE);

        TypedQuery<T> query = context.entityManager().createQuery(jpql, context.entityType());
        query.setParameter("retryableStatuses", request.retryableStatuses());
        query.setParameter("now", request.now());
        query.setParameter("processingStatus", OutboxStatus.PROCESSING);
        query.setParameter("staleBefore", request.staleBefore());
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setHint(LOCK_TIMEOUT_HINT, SKIP_LOCKED);
        query.setMaxResults(Math.max(1, request.limit()));
        List<T> lockedRows = query.getResultList();

        return lockedRows.stream()
                .map(event -> claim(event, request.now()))
                .toList();
    }

    @Override
    public <T extends OutboxEntry<ID>, ID> Optional<ClaimedOutboxEvent<ID>> claimOne(
            OutboxClaimContext<T> context,
            ID id,
            Instant now
    ) {
        EntityManager entityManager = context.entityManager();
        Map<String, Object> hints = Map.of(LOCK_TIMEOUT_HINT, SKIP_LOCKED);
        T lockedRow = entityManager.find(context.entityType(), id, LockModeType.PESSIMISTIC_WRITE, hints);
        return Optional.ofNullable(lockedRow)
                .filter(event -> isHotClaimable(event, now))
                .map(event -> claim(event, now));
    }

    private static <T extends OutboxEntry<ID>, ID> ClaimedOutboxEvent<ID> claim(T event, Instant now) {
        String claimToken = UUID.randomUUID().toString();
        event.markProcessing(now, claimToken);
        return new ClaimedOutboxEvent<>(event.getId(), claimToken);
    }

    private static boolean isHotClaimable(OutboxEntry<?> event, Instant now) {
        Instant availableAt = event.getAvailableAt();
        if (availableAt != null && availableAt.isAfter(now)) {
            return false;
        }
        return OutboxClaimJpql.HOT_CLAIMABLE_STATUSES.contains(event.getStatus());
    }
}
