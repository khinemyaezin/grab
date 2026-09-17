package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StorefrontCreatedEvent(
        String storefrontId,
        String merchantId,
        String name,
        String slug,
        String status,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
