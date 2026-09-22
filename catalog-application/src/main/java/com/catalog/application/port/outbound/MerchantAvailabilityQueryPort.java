package com.catalog.application.port.outbound;

import java.util.Optional;

public interface MerchantAvailabilityQueryPort {
    Optional<MerchantAvailabilitySlice> findByMerchantId(String merchantId);

    record MerchantAvailabilitySlice(String status, String merchantType) {
    }
}
