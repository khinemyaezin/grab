package com.grab.store.merchant.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantApprovedIntegrationEvent(
        String merchantId,
        String applicantUserId,
        String merchantName,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
