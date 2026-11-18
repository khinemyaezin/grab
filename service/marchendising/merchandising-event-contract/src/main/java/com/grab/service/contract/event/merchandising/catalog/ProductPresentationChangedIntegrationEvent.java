package com.grab.service.contract.event.merchandising.catalog;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record ProductPresentationChangedIntegrationEvent(
        String productId,
        String productName,
        Instant occurredAt,
        int version
) implements Event {
}
