package com.grab.service.contract.event.account.customer;

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
