package com.grab.outbox.infrastructure.inbox.jpa;

import com.grab.framework.inbox.ProcessedEventRepository;
import jakarta.persistence.EntityManager;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * JPA inbox. {@link #markProcessed} looks the key up first and only inserts when absent, so the common duplicate
 * path never raises a constraint violation (which would poison the surrounding PostgreSQL transaction). A true
 * concurrent duplicate loses the race on the primary key at flush, its transaction rolls back, and the outbox
 * retries it; that retry then sees the row and returns {@code false}.
 */
public class JpaProcessedEventRepository<E extends JpaProcessedEventEntity> implements ProcessedEventRepository {

    private final EntityManager entityManager;
    private final Class<E> entityType;
    private final BiFunction<ProcessedEventKey, Instant, E> factory;
    private final Clock clock;

    public JpaProcessedEventRepository(
            EntityManager entityManager,
            Class<E> entityType,
            BiFunction<ProcessedEventKey, Instant, E> factory
    ) {
        this(entityManager, entityType, factory, Clock.systemUTC());
    }

    public JpaProcessedEventRepository(
            EntityManager entityManager,
            Class<E> entityType,
            BiFunction<ProcessedEventKey, Instant, E> factory,
            Clock clock
    ) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.factory = Objects.requireNonNull(factory, "factory");
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public boolean markProcessed(String consumer, UUID eventId) {
        ProcessedEventKey key = new ProcessedEventKey(consumer, eventId);
        E existing = entityManager.find(entityType, key);
        if (existing != null) {
            return false;
        }
        Instant now = clock.instant();
        E row = factory.apply(key, now);
        entityManager.persist(row);
        entityManager.flush();
        return true;
    }

    @Override
    public int deleteProcessedBefore(Instant cutoff, int limit) {
        String entityName = entityManager.getMetamodel().entity(entityType).getName();
        List<E> expired = entityManager.createQuery(
                        "select p from " + entityName + " p where p.processedAt < :cutoff order by p.processedAt",
                        entityType)
                .setParameter("cutoff", cutoff)
                .setMaxResults(Math.max(1, limit))
                .getResultList();
        expired.forEach(entityManager::remove);
        entityManager.flush();
        return expired.size();
    }
}
