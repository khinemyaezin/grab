package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.MerchantAvailabilityQueryPort;
import com.catalog.adapter.persistence.repository.CatalogMerchantAvailabilityJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class MerchantAvailabilityQueryAdapter implements MerchantAvailabilityQueryPort {

    private final CatalogMerchantAvailabilityJpaRepository availability;

    @Override
    public Optional<MerchantAvailabilitySlice> findByMerchantId(String merchantId) {
        return availability.findByMerchantId(merchantId)
                .map(entity -> new MerchantAvailabilitySlice(entity.getStatus(), entity.getMerchantType()));
    }
}
