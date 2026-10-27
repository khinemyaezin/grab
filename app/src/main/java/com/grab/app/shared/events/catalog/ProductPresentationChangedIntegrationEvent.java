package com.grab.app.shared.events.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductPresentationChangedIntegrationEvent(
        String productId,
        String productName,
        Instant occurredAt,
        int version
) implements Event {
}
