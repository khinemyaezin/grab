package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.OutboxStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JpaOutboxStore<T extends OutboxEntry<ID>, ID> implements OutboxStore<T, ID> {

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
    }

    @Override
    public List<T> findBatchForProcessing(
            Collection<OutboxStatus> retryableStatuses,
            OutboxStatus processingStatus,
            LocalDateTime availableAt,
            LocalDateTime staleBefore,
            int batchSize
    ) {
        String jpql = """
                select event
                from %s event
                where
                    (event.status in :retryableStatuses and event.availableAt <= :availableAt)
                    or (event.status = :processingStatus and event.claimedAt <= :staleBefore)
                order by event.occurredAt asc
                """.formatted(entityName);

        TypedQuery<T> query = entityManager.createQuery(jpql, entityType);
        query.setParameter("retryableStatuses", retryableStatuses);
        query.setParameter("processingStatus", processingStatus);
        query.setParameter("availableAt", availableAt);
        query.setParameter("staleBefore", staleBefore);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setMaxResults(batchSize);
        return query.getResultList();
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entityManager.find(entityType, id));
    }

    @Override
    public void deletePublishedOlderThan(LocalDateTime cutoff) {
        String jpql = """
                delete
                from %s event
                where event.status = :status
                  and event.publishedAt < :cutoff
                """.formatted(entityName);

        entityManager.createQuery(jpql)
                .setParameter("status", OutboxStatus.PUBLISHED)
                .setParameter("cutoff", cutoff)
                .executeUpdate();
    }
}
