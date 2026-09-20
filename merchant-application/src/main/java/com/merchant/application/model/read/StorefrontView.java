package com.merchant.application.model.read;

import com.merchant.domain.enums.StorefrontStatus;

import java.time.Instant;

public record StorefrontView(
        String id,
        String merchantId,
        String salesChannelId,
        String name,
        String slug,
        StorefrontStatus status,
        String lifecycleReason,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
