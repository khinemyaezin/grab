package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.Set;

public record MerchantMemberCreatedEvent(
        String memberId,
        String merchantId,
        String userId,
        String role,
        Set<String> authorities,
        boolean isAdmin,
        String status,
        String invitedBy,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
