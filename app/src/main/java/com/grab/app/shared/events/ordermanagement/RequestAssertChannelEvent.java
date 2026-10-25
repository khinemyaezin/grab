package com.grab.app.shared.events.ordermanagement;

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
