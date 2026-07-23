package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record SellableProductProductCreatedEvent(
        String workflowId,
        String productId,
        List<String> skus,
        Instant occurredAt,
        int version
) implements Event {
}
