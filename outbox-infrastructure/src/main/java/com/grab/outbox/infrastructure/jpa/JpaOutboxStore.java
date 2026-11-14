package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.OutboxStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaOutboxStore<T extends OutboxEntry<ID>, ID> implements OutboxStore<T, ID> {

    static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";
    /**
     * Hibernate's {@code LockOptions.SKIP_LOCKED}; rendered as {@code FOR UPDATE SKIP LOCKED} on PostgreSQL.
     */
    static final int SKIP_LOCKED = -2;

    private final EntityManager entityManager;
    private final Class<T> entityType;
    private final String entityName;

    public JpaOutboxStore(EntityManager entityManager, Class<T> entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
        this.entityName = entityManager.getMetamodel().entity(entityType).getName();
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
    public List<T> findBatchForProcessing(
            Collection<OutboxStatus> retryableStatuses,
            OutboxStatus processingStatus,
            Instant availableAt,
            Instant staleBefore,
            int batchSize
    ) {
        String jpql = """
                select event
                from %s event
                where
                    (event.status in :retryableStatuses and event.availableAt <= :availableAt)
                    or (event.status = :processingStatus and event.claimedAt <= :staleBefore)
                order by event.id asc
                """.formatted(entityName);

        TypedQuery<T> query = entityManager.createQuery(jpql, entityType);
        query.setParameter("retryableStatuses", retryableStatuses);
        query.setParameter("processingStatus", processingStatus);
        query.setParameter("availableAt", availableAt);
        query.setParameter("staleBefore", staleBefore);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setHint(LOCK_TIMEOUT_HINT, SKIP_LOCKED);
        query.setMaxResults(batchSize);
        return query.getResultList();
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entityManager.find(entityType, id));
    }

    @Override
    public Optional<T> findByIdForUpdateSkipLocked(ID id) {
        Map<String, Object> hints = Map.of(LOCK_TIMEOUT_HINT, SKIP_LOCKED);
        return Optional.ofNullable(entityManager.find(entityType, id, LockModeType.PESSIMISTIC_WRITE, hints));
    }

    @Override
    public Optional<T> findByIdForUpdate(ID id) {
        T managed = entityManager.find(entityType, id);
        if (managed == null) {
            return Optional.empty();
        }
        // refresh (not find) so a row already in the persistence context is re-read under the lock
        entityManager.refresh(managed, LockModeType.PESSIMISTIC_WRITE);
        return Optional.of(managed);
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
                """.formatted(entityName);

        List<?> ids = entityManager.createQuery(selectJpql)
                .setParameter("status", OutboxStatus.PUBLISHED)
                .setParameter("cutoff", cutoff)
                .setMaxResults(Math.max(1, limit))
                .getResultList();
        if (ids.isEmpty()) {
            return 0;
        }

        String deleteJpql = "delete from %s event where event.id in :ids".formatted(entityName);
        return entityManager.createQuery(deleteJpql)
                .setParameter("ids", ids)
                .executeUpdate();
    }
}
