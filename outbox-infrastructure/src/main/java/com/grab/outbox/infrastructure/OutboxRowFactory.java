package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.SerializedEvent;

import java.time.LocalDateTime;

@FunctionalInterface
public interface OutboxRowFactory<T> {
    T create(
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            LocalDateTime occurredAt
    );
}
