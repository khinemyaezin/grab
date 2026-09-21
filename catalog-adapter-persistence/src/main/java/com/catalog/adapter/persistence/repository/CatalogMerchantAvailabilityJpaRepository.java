package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CatalogMerchantAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogMerchantAvailabilityJpaRepository
        extends JpaRepository<CatalogMerchantAvailabilityEntity, String> {
    Optional<CatalogMerchantAvailabilityEntity> findByMerchantId(String merchantId);
}
