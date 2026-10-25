package com.grab.app.shared.events.ordermanagement;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestDeleteProductCompensationEvent(
        String workflowId,
        String merchantId,
        String productId,
        Instant occurredAt,
        int version
) implements Event {
}
