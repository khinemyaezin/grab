package com.grab.app.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record StorefrontStatusChangedIntegrationEvent(
        String storefrontId,
        String merchantId,
        String slug,
        String previousStatus,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
