package com.catalog.domain.port.outbound;

public interface MerchantAvailabilityRepository {
    void upsert(String merchantId, String status, String merchantType);
}
