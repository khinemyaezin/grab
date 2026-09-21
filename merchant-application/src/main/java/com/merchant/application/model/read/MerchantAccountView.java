package com.merchant.application.model.read;

import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;

import java.time.Instant;

public record MerchantAccountView(
        String merchantId,
        String applicantUserId,
        MerchantType type,
        String legalName,
        String displayName,
        String registrationCountryCode,
        String registrationNumber,
        String contactEmail,
        String contactPhone,
        String addressLine1,
        String addressLine2,
        String addressCity,
        String addressRegion,
        String addressPostalCode,
        String addressCountryCode,
        MerchantStatus status,
        String lifecycleReason,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
