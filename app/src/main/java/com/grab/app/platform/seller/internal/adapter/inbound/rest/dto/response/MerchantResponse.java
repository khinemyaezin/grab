package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.time.Instant;

public record MerchantResponse(
        String merchantId,
        String businessType,
        String status,
        String legalName,
        String displayName,
        Instant createdAt,
        Instant updatedAt
) {}
