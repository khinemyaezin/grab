package com.identity.adapter.persistence.specification.jpa;

import com.identity.adapter.persistence.entity.SecurityManifestRevisionEntity;
import com.identity.application.model.read.WaitingSecurityManifestView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class SecurityManifestWaitingSpecification {
    private final EntityManager entityManager;

    public List<WaitingSecurityManifestView> findWaiting(int limit) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createQuery(WaitingSecurityManifestView.class);
        var revision = query.from(SecurityManifestRevisionEntity.class);
        var selection = builder.construct(WaitingSecurityManifestView.class,
                revision.get("moduleKey"), revision.get("revision"));
        var waiting = builder.equal(revision.get("status"), "WAITING_DEPENDENCY");
        query.select(selection).where(waiting).orderBy(builder.asc(revision.get("receivedAt")), builder.asc(revision.get("id")));
        var typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(limit);
        return typedQuery.getResultList();
    }
}
