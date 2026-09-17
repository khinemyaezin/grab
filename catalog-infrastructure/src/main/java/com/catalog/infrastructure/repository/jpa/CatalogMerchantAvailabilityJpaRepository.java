package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CatalogMerchantAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogMerchantAvailabilityJpaRepository
        extends JpaRepository<CatalogMerchantAvailabilityEntity, String> {
    Optional<CatalogMerchantAvailabilityEntity> findByMerchantId(String merchantId);
}
