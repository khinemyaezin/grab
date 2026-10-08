package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantMemberRemovedEvent(
        String memberId,
        String merchantId,
        String userId,
        String role,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
