package com.grab.store.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantApplicationStartedIntegrationEvent(
        String merchantId,
        String merchantName,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
