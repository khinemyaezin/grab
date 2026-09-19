package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.List;

public record RequestReplaceProductDescriptionsEvent(
        String workflowId,
        String merchantId,
        String productId,
        List<Description> descriptions,
        Instant occurredAt,
        int version
) implements Event {

    public RequestReplaceProductDescriptionsEvent {
        descriptions = descriptions == null ? List.of() : List.copyOf(descriptions);
    }

    public record Description(
            String id,
            String name,
            String title,
            String description
    ) {
    }
}
