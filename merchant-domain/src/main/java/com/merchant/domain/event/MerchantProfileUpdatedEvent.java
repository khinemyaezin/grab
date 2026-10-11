package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantProfileUpdatedEvent(
        String merchantId,
        String merchantName,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
