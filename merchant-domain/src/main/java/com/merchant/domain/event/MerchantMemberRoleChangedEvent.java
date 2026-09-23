package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantMemberRoleChangedEvent(
        String memberId,
        String merchantId,
        String userId,
        String previousRole,
        String newRole,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
