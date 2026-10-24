package com.manifest.adapter.persistence.adapter;

import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import com.grab.framework.security.SecurityManifestBacklogView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

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
        SecurityManifestPublicationState state = entityManager.find(stateType, moduleKey);
        String jpql = "select new com.grab.framework.security.SecurityManifestBacklogView(count(o), min(o.occurredAt)) from " + outboxEntityName
                + " o where o.aggregateType = :aggregateType and o.status <> :published";
        TypedQuery<SecurityManifestBacklogView> query = entityManager.createQuery(jpql, SecurityManifestBacklogView.class);
        query.setParameter("aggregateType", "SecurityManifest");
        query.setParameter("published", OutboxStatus.PUBLISHED);
        SecurityManifestBacklogView backlog = query.getSingleResult();
        long pending = backlog.pendingCount();
        String oldest = backlog.oldestOccurredAt() == null ? null : backlog.oldestOccurredAt().toString();
        int revision = state == null ? 0 : state.revision();
        String digest = state == null ? null : state.digest();
        String lastEnqueued = state == null || state.lastEnqueuedAt() == null ? null : state.lastEnqueuedAt().toString();
        return new PublicationStatus(moduleKey, revision, digest, lastEnqueued, pending, oldest);
    }
}
