package com.catalog.application.port.outbound;

import java.util.Optional;

public interface MerchantAvailabilityPort {
    void upsert(String merchantId, String status, String merchantType);

    Optional<MerchantAvailabilitySlice> findByMerchantId(String merchantId);

    record MerchantAvailabilitySlice(String status, String merchantType) {
    }
}
