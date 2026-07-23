package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record SellableProductStepFailedEvent(
        String workflowId,
        String step,
        String message,
        Instant occurredAt,
        int version
) implements Event {
}
