package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StorefrontRenamedEvent(
        String storefrontId,
        String merchantId,
        String previousName,
        String name,
        String previousSlug,
        String slug,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
