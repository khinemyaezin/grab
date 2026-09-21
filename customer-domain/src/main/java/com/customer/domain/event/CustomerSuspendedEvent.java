package com.customer.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record CustomerSuspendedEvent(
        String customerId,
        Instant occurredAt
) implements Event {
}
