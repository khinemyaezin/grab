package com.grab.framework.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxDispatchMetadata(
        UUID eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        int eventVersion,
        Instant occurredAt,
        String payload,
        String headers
) {
}
