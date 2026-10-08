package com.grab.store.customer.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record GuestCustomerCreatedIntegrationEvent(
        String customerId,
        String email,
        String displayName,
        Instant occurredAt,
        int version
) implements Event {
}
