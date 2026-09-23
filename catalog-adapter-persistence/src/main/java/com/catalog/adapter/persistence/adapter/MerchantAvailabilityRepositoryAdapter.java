package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CatalogMerchantAvailabilityEntity;
import com.catalog.adapter.persistence.repository.CatalogMerchantAvailabilityJpaRepository;
import com.catalog.domain.port.outbound.MerchantAvailabilityRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class MerchantAvailabilityRepositoryAdapter implements MerchantAvailabilityRepository {

    private final CatalogMerchantAvailabilityJpaRepository availability;

    @Override
    public void upsert(String merchantId, String status, String merchantType) {
        CatalogMerchantAvailabilityEntity entity = availability.findByMerchantId(merchantId)
                .orElseGet(CatalogMerchantAvailabilityEntity::new);
        entity.setMerchantId(merchantId);
        entity.setStatus(status);
        if (merchantType != null && !merchantType.isBlank()) {
            entity.setMerchantType(merchantType);
        }
        entity.setUpdatedAt(Instant.now());
        availability.save(entity);
    }
}
