package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestApplyProductStatusEvent(
        String workflowId,
        String merchantId,
        String productId,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
