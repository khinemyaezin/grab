package com.customer.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record CustomerAttachedToUserEvent(
        String customerId,
        String userId,
        Instant occurredAt
) implements Event {
}
