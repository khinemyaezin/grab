package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StorefrontStatusChangedEvent(
        String storefrontId,
        String merchantId,
        String slug,
        String previousStatus,
        String status,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
