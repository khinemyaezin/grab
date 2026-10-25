package com.grab.app.shared.events.ordermanagement;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record RequestDeletePriceSetCompensationEvent(
        String workflowId,
        String priceSetId,
        Instant occurredAt,
        int version
) implements Event {
}
