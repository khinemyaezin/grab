package com.grab.app.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StorefrontRenamedIntegrationEvent(
        String storefrontId,
        String merchantId,
        String name,
        String slug,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
