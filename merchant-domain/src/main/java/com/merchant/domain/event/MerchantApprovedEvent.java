package com.merchant.domain.event;

import java.time.Instant;

public record MerchantApprovedEvent(
        String merchantId,
        String merchantName,
        String applicantUserId,
        String status,
        String actorId,
        long aggregateVersion,
        Instant occurredAt)
        implements MerchantLifecycleEvent {
}
