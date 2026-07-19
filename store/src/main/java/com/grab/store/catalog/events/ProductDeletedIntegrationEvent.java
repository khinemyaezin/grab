package com.grab.store.catalog.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record ProductDeletedIntegrationEvent(
        String productId,
        List<String> variantIds,
        Instant occurredAt,
        int version
) implements Event {
}
