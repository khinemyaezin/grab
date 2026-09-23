package com.customer.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record GuestCustomerCreatedEvent(
        String customerId,
        String email,
        String displayName,
        Instant occurredAt
) implements Event {
}
