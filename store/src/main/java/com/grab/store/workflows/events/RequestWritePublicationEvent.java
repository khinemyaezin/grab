package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestWritePublicationEvent(
        String workflowId,
        String merchantId,
        String productId,
        String variantId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
