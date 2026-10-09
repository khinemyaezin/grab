package com.grab.store.shared.events.identity;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.Objects;

public record MerchantAdminAccessProvisionedIntegrationEvent(
        String eventId,
        String requestId,
        String merchantId,
        String applicantUserId,
        String roleCode,
        String scopeKey,
        String outcome,
        String assignmentId,
        String errorCode,
        long memberVersion,
        Instant completedAt
) implements Event {
    public MerchantAdminAccessProvisionedIntegrationEvent {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(applicantUserId, "applicantUserId is required");
        Objects.requireNonNull(roleCode, "roleCode is required");
        Objects.requireNonNull(scopeKey, "scopeKey is required");
        Objects.requireNonNull(outcome, "outcome is required");
        if (completedAt == null) {
            completedAt = Instant.now();
        }
    }
}
