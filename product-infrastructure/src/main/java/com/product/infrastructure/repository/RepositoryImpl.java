package com.product.infrastructure.repository;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class RepositoryImpl <T, ID> extends SimpleJpaRepository<T, ID> {
    private final EntityManager entityManager;

    RepositoryImpl(JpaEntityInformation entityInformation,
                   EntityManager entityManager) {
        super(entityInformation, entityManager);

        // Keep the EntityManager around to used from the newly introduced methods.
        this.entityManager = entityManager;
    }
}
