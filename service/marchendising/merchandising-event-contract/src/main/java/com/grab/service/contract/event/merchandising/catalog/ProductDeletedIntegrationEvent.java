package com.grab.service.contract.event.merchandising.catalog;

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
