package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.SerializedEvent;

import java.time.Instant;
import java.util.UUID;

@FunctionalInterface
public interface OutboxRowFactory<T> {
    T create(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            Instant occurredAt
    );
}
