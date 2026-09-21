package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.MerchantAvailabilityPort;
import com.catalog.adapter.persistence.entity.CatalogMerchantAvailabilityEntity;
import com.catalog.adapter.persistence.repository.CatalogMerchantAvailabilityJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class MerchantAvailabilityAdapter implements MerchantAvailabilityPort {

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

    @Override
    public Optional<MerchantAvailabilitySlice> findByMerchantId(String merchantId) {
        return availability.findByMerchantId(merchantId)
                .map(entity -> new MerchantAvailabilitySlice(entity.getStatus(), entity.getMerchantType()));
    }
}
