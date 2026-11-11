package com.grab.app.shared.events.customer;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record CustomerRegisteredIntegrationEvent(
        String customerId,
        String userId,
        String email,
        String displayName,
        Instant occurredAt,
        int version
) implements Event {
}
