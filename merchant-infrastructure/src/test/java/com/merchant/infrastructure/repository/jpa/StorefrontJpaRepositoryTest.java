package com.merchant.infrastructure.repository.jpa;

import com.merchant.domain.enums.StorefrontStatus;
import com.merchant.infrastructure.entity.StorefrontEntity;
import com.merchant.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorefrontJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private StorefrontJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(storefront("sf-1", "merchant-1", "main-shop"));
    }

    @Test
    void existsBySlug_withExistingSlug_shouldReturnTrue() {
        assertTrue(repository.existsBySlug("main-shop"));
    }

    @Test
    void save_withDuplicateSlug_shouldFail() {
        assertThrows(ConstraintViolationException.class, () ->
                repository.saveAndFlush(storefront("sf-2", "merchant-2", "main-shop")));
    }

    private StorefrontEntity storefront(String uuid, String merchantId, String slug) {
        StorefrontEntity entity = new StorefrontEntity();
        entity.setUuid(uuid);
        entity.setMerchantId(merchantId);
        entity.setName("Shop");
        entity.setSlug(slug);
        entity.setStatus(StorefrontStatus.DRAFT);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setVersion(0L);
        return entity;
    }
}
