package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.outbox.ClaimedOutboxEvent;
import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.OutboxBacklog;
import com.grab.outbox.infrastructure.OutboxClaimRequest;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.claim.OutboxClaimContext;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategies;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPA outbox store for one module. Claiming is delegated to the configured {@link OutboxClaimStrategy}
 * ({@code <module>.outbox.claim-strategy}); everything else is portable JPQL.
 */
public class JpaOutboxStore<T extends OutboxEntry<ID>, ID> implements OutboxStore<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> entityType;
    private final OutboxClaimContext<T> claimContext;
    private final OutboxClaimStrategy claimStrategy;

    /**
     * Uses the default {@code skip-locked} claim strategy.
     */
    public JpaOutboxStore(EntityManager entityManager, Class<T> entityType) {
        this(entityManager, entityType, OutboxClaimStrategies.defaultStrategy());
    }

    public JpaOutboxStore(EntityManager entityManager, Class<T> entityType, OutboxClaimStrategy claimStrategy) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.claimStrategy = Objects.requireNonNull(claimStrategy, "claimStrategy");
        String entityName = entityManager.getMetamodel().entity(entityType).getName();
        this.claimContext = new OutboxClaimContext<>(entityManager, entityType, entityName);
    }

    public OutboxClaimStrategy claimStrategy() {
        return claimStrategy;
    }

    public Class<T> entityType() {
        return entityType;
    }

    public EntityManagerFactory entityManagerFactory() {
        return entityManager.getEntityManagerFactory();
    }

    @Override
    public void saveAll(Collection<T> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        events.forEach(entityManager::persist);
        entityManager.flush();
    }

    @Override
    public List<ClaimedOutboxEvent<ID>> claimBatch(OutboxClaimRequest request) {
        return claimStrategy.claimBatch(claimContext, request);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entityManager.find(entityType, id));
    }

    @Override
    public Optional<ClaimedOutboxEvent<ID>> claimOne(ID id, Instant now) {
        return claimStrategy.claimOne(claimContext, id, now);
    }

    @Override
    public Optional<T> findByIdForUpdate(ID id) {
        return claimStrategy.lockForOutcome(claimContext, id);
    }

    @Override
    public OutboxBacklog backlog(Collection<OutboxStatus> pendingStatuses) {
        String jpql = """
                select count(event), min(event.occurredAt)
                from %s event
                where event.status in :statuses
                """.formatted(claimContext.entityName());
        Object[] row = entityManager.createQuery(jpql, Object[].class)
                .setParameter("statuses", pendingStatuses)
                .getSingleResult();
        long pending = row[0] == null ? 0L : ((Number) row[0]).longValue();
        Instant oldest = (Instant) row[1];
        return new OutboxBacklog(pending, oldest);
    }

    @Override
    public List<T> findDeadForReplay(ID fromId, ID toId, int limit) {
        String jpql = """
                select event
                from %s event
                where event.status = :status
                  and event.id between :fromId and :toId
                order by event.id asc
                """.formatted(claimContext.entityName());
        TypedQuery<T> query = entityManager.createQuery(jpql, entityType);
        query.setParameter("status", OutboxStatus.DEAD);
        query.setParameter("fromId", fromId);
        query.setParameter("toId", toId);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setMaxResults(Math.max(1, limit));
        return query.getResultList();
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public int deletePublishedOlderThan(Instant cutoff, int limit) {
        String selectJpql = """
                select event.id
                from %s event
                where event.status = :status
                  and event.publishedAt < :cutoff
                order by event.id asc
                """.formatted(claimContext.entityName());

        List<?> ids = entityManager.createQuery(selectJpql)
                .setParameter("status", OutboxStatus.PUBLISHED)
                .setParameter("cutoff", cutoff)
                .setMaxResults(Math.max(1, limit))
                .getResultList();
        if (ids.isEmpty()) {
            return 0;
        }

        String deleteJpql = "delete from %s event where event.id in :ids".formatted(claimContext.entityName());
        return entityManager.createQuery(deleteJpql)
                .setParameter("ids", ids)
                .executeUpdate();
    }
}
