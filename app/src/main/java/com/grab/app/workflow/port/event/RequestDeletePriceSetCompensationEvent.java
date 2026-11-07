package com.grab.app.workflow.port.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestDeletePriceSetCompensationEvent(
        String workflowId,
        String priceSetId,
        Instant occurredAt,
        int version
) implements Event {
}
