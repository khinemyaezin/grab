package com.manifest.adapter.persistence.adapter;

import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import jakarta.persistence.EntityManager;

import java.util.Objects;
import java.util.function.Function;

public final class SecurityManifestPublicationStateProvider {

    private SecurityManifestPublicationStateProvider() {
    }

    public static <E extends SecurityManifestPublicationState> Function<String, E> optimisticProvider(
            EntityManager entityManager,
            Class<E> entityType,
            Function<String, E> entityFactory) {
        Objects.requireNonNull(entityManager, "entityManager must not be null");
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(entityFactory, "entityFactory must not be null");

        return moduleKey -> {
            E existing = entityManager.find(entityType, moduleKey);
            if (existing != null) {
                return existing;
            }

            E fresh = entityFactory.apply(moduleKey);
            entityManager.persist(fresh);
            entityManager.flush();
            return fresh;
        };
    }
}
