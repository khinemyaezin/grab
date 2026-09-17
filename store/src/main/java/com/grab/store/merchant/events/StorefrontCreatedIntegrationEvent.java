package com.grab.store.merchant.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StorefrontCreatedIntegrationEvent(
        String storefrontId,
        String merchantId,
        String name,
        String slug,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
