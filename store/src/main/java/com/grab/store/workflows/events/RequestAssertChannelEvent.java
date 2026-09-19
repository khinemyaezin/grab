package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestAssertChannelEvent(
        String workflowId,
        String merchantId,
        String salesChannelId,
        Instant occurredAt,
        int version
) implements Event {
}
