package com.customer.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record CustomerRegisteredEvent(
        String customerId,
        String userId,
        String email,
        String displayName,
        Instant occurredAt
) implements Event {
}
