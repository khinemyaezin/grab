package com.grab.store.shared.events.identity;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record UserRegisteredIntegrationEvent(
        String userId,
        String email,
        Instant occurredAt,
        int version
) implements Event {
}
