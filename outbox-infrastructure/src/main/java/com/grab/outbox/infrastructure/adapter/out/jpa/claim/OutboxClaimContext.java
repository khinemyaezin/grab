package com.grab.outbox.infrastructure.adapter.out.jpa.claim;

import jakarta.persistence.EntityManager;

import java.util.Objects;

/**
 * The module's outbox table as seen by a claim strategy: the (module-scoped) entity manager, the concrete outbox
 * entity class and its JPQL entity name.
 */
public record OutboxClaimContext<T>(EntityManager entityManager, Class<T> entityType, String entityName) {

    public OutboxClaimContext {
        Objects.requireNonNull(entityManager, "entityManager");
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(entityName, "entityName");
    }
}
