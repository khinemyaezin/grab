package com.grab.app.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantApprovedIntegrationEvent(
        String merchantId,
        String primaryMarketCountryCode,
        Instant occurredAt,
        int version
) implements Event {
}
