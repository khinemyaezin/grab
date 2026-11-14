package com.grab.outbox.infrastructure.claim;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.OutboxClaimRequest;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Portable claim that needs no skip-locked support, only standard JPQL:
 * <ol>
 *   <li>select candidate ids without a lock from a window of {@code limit × candidateWindowFactor} claimable rows
 *       and pick a random {@code limit} of them, so concurrent workers rarely race for the same head-of-queue
 *       rows;</li>
 *   <li>bulk {@code UPDATE … SET status = PROCESSING, claimToken = :token … WHERE id IN :ids AND <claimable>};
 *       the claimable predicate is re-evaluated against the latest committed row, so a row another worker
 *       claimed in the meantime is not updated;</li>
 *   <li>select the ids that now carry {@code :token}: exactly the rows this worker won.</li>
 * </ol>
 *
 * <p>A concurrent update of the same row waits only for the other worker's short claim transaction. Works on
 * H2, MySQL 5.7, MariaDB &lt; 10.6 and any other JPA database. Bulk JPQL bypasses the persistence context; claim
 * transactions load no outbox entities, and outcome updates re-read rows under a lock, so nothing goes stale.</p>
 */
public final class ConditionalUpdateClaimStrategy implements OutboxClaimStrategy {

    public static final String NAME = "conditional-update";
    static final int DEFAULT_CANDIDATE_WINDOW_FACTOR = 4;

    private final int candidateWindowFactor;

    public ConditionalUpdateClaimStrategy() {
        this(DEFAULT_CANDIDATE_WINDOW_FACTOR);
    }

    public ConditionalUpdateClaimStrategy(int candidateWindowFactor) {
        if (candidateWindowFactor < 1) {
            throw new IllegalArgumentException("candidateWindowFactor must be >= 1");
        }
        this.candidateWindowFactor = candidateWindowFactor;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public <T extends OutboxEntry<ID>, ID> List<ClaimedOutboxEvent<ID>> claimBatch(
            OutboxClaimContext<T> context,
            OutboxClaimRequest request
    ) {
        int limit = Math.max(1, request.limit());
        List<ID> candidates = selectCandidates(context, request, limit);
        if (candidates.isEmpty()) {
            return List.of();
        }

        String claimToken = UUID.randomUUID().toString();
        String updateJpql = """
                update %s event
                set event.status = :claimedStatus,
                    event.claimToken = :claimToken,
                    event.claimedAt = :now,
                    event.attemptCount = event.attemptCount + 1
                where event.id in :ids
                  and %s
                """.formatted(context.entityName(), OutboxClaimJpql.BATCH_CLAIMABLE);
        int updated = context.entityManager().createQuery(updateJpql)
                .setParameter("claimedStatus", OutboxStatus.PROCESSING)
                .setParameter("claimToken", claimToken)
                .setParameter("ids", candidates)
                .setParameter("retryableStatuses", request.retryableStatuses())
                .setParameter("now", request.now())
                .setParameter("processingStatus", OutboxStatus.PROCESSING)
                .setParameter("staleBefore", request.staleBefore())
                .executeUpdate();
        if (updated == 0) {
            return List.of();
        }

        List<ID> wonIds = selectClaimedIds(context, candidates, claimToken);
        return wonIds.stream()
                .map(id -> new ClaimedOutboxEvent<>(id, claimToken))
                .toList();
    }

    @Override
    public <T extends OutboxEntry<ID>, ID> Optional<ClaimedOutboxEvent<ID>> claimOne(
            OutboxClaimContext<T> context,
            ID id,
            Instant now
    ) {
        String claimToken = UUID.randomUUID().toString();
        String updateJpql = """
                update %s event
                set event.status = :claimedStatus,
                    event.claimToken = :claimToken,
                    event.claimedAt = :now,
                    event.attemptCount = event.attemptCount + 1
                where event.id = :id
                  and %s
                """.formatted(context.entityName(), OutboxClaimJpql.HOT_CLAIMABLE);
        int updated = context.entityManager().createQuery(updateJpql)
                .setParameter("claimedStatus", OutboxStatus.PROCESSING)
                .setParameter("claimToken", claimToken)
                .setParameter("id", id)
                .setParameter("hotStatuses", OutboxClaimJpql.HOT_CLAIMABLE_STATUSES)
                .setParameter("now", now)
                .executeUpdate();
        if (updated == 0) {
            return Optional.empty();
        }
        return Optional.of(new ClaimedOutboxEvent<>(id, claimToken));
    }

    @SuppressWarnings("unchecked")
    private <T extends OutboxEntry<ID>, ID> List<ID> selectCandidates(
            OutboxClaimContext<T> context,
            OutboxClaimRequest request,
            int limit
    ) {
        String selectJpql = """
                select event.id
                from %s event
                where %s
                order by event.id asc
                """.formatted(context.entityName(), OutboxClaimJpql.BATCH_CLAIMABLE);
        EntityManager entityManager = context.entityManager();
        List<ID> window = (List<ID>) entityManager.createQuery(selectJpql)
                .setParameter("retryableStatuses", request.retryableStatuses())
                .setParameter("now", request.now())
                .setParameter("processingStatus", OutboxStatus.PROCESSING)
                .setParameter("staleBefore", request.staleBefore())
                .setMaxResults(limit * candidateWindowFactor)
                .getResultList();
        if (window.size() <= limit) {
            return window;
        }
        List<ID> shuffled = new ArrayList<>(window);
        Collections.shuffle(shuffled, ThreadLocalRandom.current());
        List<ID> picked = new ArrayList<>(shuffled.subList(0, limit));
        picked.sort(idOrder());
        return picked;
    }

    @SuppressWarnings("unchecked")
    private <T extends OutboxEntry<ID>, ID> List<ID> selectClaimedIds(
            OutboxClaimContext<T> context,
            List<ID> candidates,
            String claimToken
    ) {
        String selectJpql = """
                select event.id
                from %s event
                where event.id in :ids
                  and event.claimToken = :claimToken
                order by event.id asc
                """.formatted(context.entityName());
        return (List<ID>) context.entityManager().createQuery(selectJpql)
                .setParameter("ids", candidates)
                .setParameter("claimToken", claimToken)
                .getResultList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <ID> Comparator<ID> idOrder() {
        return (left, right) -> left instanceof Comparable comparable ? comparable.compareTo(right) : 0;
    }
}
