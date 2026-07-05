package com.grab.store.merchant.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantApprovedIntegrationEvent(
        String eventId,
        String merchantId,
        String applicantUserId,
        Instant occurredAt,
        int version
) implements Event {
}
