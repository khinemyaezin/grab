package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response;

import java.time.Instant;

public record StorefrontResponse(
        String storefrontId,
        String merchantId,
        String name,
        String slug,
        String status,
        String lifecycleReason,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
