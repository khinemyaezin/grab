package com.grab.store.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantApprovedIntegrationEvent(
        String merchantId,
        Instant occurredAt,
        int version
) implements Event {
}
