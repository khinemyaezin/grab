package com.grab.store.merchant.internal.api.rest.dto.response;

import java.time.Instant;

public record MerchantResponse(
        String merchantId,
        String applicantUserId,
        String type,
        String legalName,
        String displayName,
        Registration registration,
        Contact contact,
        RegisteredAddress registeredAddress,
        String status,
        String lifecycleReason,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public record Registration(String countryCode, String registrationNumber) {
    }

    public record Contact(String email, String phone) {
    }

    public record RegisteredAddress(
            String line1, String line2, String city, String region, String postalCode, String countryCode
    ) {
    }
}
