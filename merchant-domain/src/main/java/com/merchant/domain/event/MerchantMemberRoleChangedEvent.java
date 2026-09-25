package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.Set;

public record MerchantMemberRoleChangedEvent(
        String memberId,
        String merchantId,
        String userId,
        String previousRole,
        String newRole,
        Set<String> authorities,
        long aggregateVersion,
        Instant occurredAt
) implements Event {

    public MerchantMemberRoleChangedEvent(
            String memberId,
            String merchantId,
            String userId,
            String previousRole,
            String newRole,
            long aggregateVersion,
            Instant occurredAt
    ) {
        this(memberId, merchantId, userId, previousRole, newRole, Set.of(), aggregateVersion, occurredAt);
    }
}
