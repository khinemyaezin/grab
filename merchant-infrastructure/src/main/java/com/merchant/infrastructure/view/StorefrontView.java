package com.merchant.infrastructure.view;

import com.merchant.domain.enums.StorefrontStatus;

import java.time.Instant;

public record StorefrontView(
        String id,
        String merchantId,
        String name,
        String slug,
        StorefrontStatus status,
        String lifecycleReason,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
