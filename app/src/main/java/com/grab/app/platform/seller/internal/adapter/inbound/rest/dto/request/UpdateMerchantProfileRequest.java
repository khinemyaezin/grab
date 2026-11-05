package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

public record UpdateMerchantProfileRequest(
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
        String addressCountryCode
) {}
