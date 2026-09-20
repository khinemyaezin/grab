package com.grab.store.pricing.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record PriceSetChangedIntegrationEvent(
        String priceSetId,
        Instant occurredAt,
        int version
) implements Event {
}
