package com.grab.store.shared.events.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.Objects;

public record MerchantAdminAccessProvisionRequestedIntegrationEvent(
        String eventId,
        String merchantId,
        String applicantUserId,
        String roleCode,
        String scopeKey,
        long memberVersion,
        Instant requestedAt
) implements Event {
    public MerchantAdminAccessProvisionRequestedIntegrationEvent {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(applicantUserId, "applicantUserId is required");
        Objects.requireNonNull(roleCode, "roleCode is required");
        Objects.requireNonNull(scopeKey, "scopeKey is required");
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }
}
