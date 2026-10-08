package com.grab.outbox.infrastructure.security;

import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import jakarta.persistence.EntityManager;

public class SecurityManifestPublicationQueryAdapter implements SecurityManifestPublicationQueryPort {
    private final EntityManager entityManager;
    private final Class<? extends SecurityManifestPublicationState> stateType;
    private final String outboxEntityName;

    public SecurityManifestPublicationQueryAdapter(EntityManager entityManager,
            Class<? extends SecurityManifestPublicationState> stateType, String outboxEntityName) {
        this.entityManager = entityManager;
        this.stateType = stateType;
        this.outboxEntityName = outboxEntityName;
    }

    @Override
    public PublicationStatus status(String moduleKey) {
        var state = entityManager.find(stateType, moduleKey);
        String jpql = "select count(o), min(o.occurredAt) from " + outboxEntityName
                + " o where o.aggregateType = :aggregateType and o.status <> :published";
        var query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("aggregateType", "SecurityManifest");
        query.setParameter("published", OutboxStatus.PUBLISHED);
        Object[] backlog = query.getSingleResult();
        long pending = ((Number) backlog[0]).longValue();
        String oldest = backlog[1] == null ? null : backlog[1].toString();
        int revision = state == null ? 0 : state.revision();
        String digest = state == null ? null : state.digest();
        String lastEnqueued = state == null || state.lastEnqueuedAt() == null ? null : state.lastEnqueuedAt().toString();
        return new PublicationStatus(moduleKey, revision, digest, lastEnqueued, pending, oldest);
    }
}
