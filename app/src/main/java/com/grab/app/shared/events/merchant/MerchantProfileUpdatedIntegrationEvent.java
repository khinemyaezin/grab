package com.grab.app.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantProfileUpdatedIntegrationEvent(
        String merchantId,
        String merchantName,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
