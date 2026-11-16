package com.grab.app.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantClosedIntegrationEvent(
        String merchantId,
        String applicantUserId,
        String merchantName,
        String merchantType,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
