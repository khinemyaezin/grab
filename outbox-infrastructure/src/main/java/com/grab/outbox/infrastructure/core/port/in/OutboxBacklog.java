package com.grab.outbox.infrastructure.core.port.in;

import java.time.Duration;
import java.time.Instant;

/**
 * Snapshot of a module's undelivered outbox rows.
 *
 * @param pending          rows in NEW, FAILED or PROCESSING
 * @param oldestOccurredAt {@code occurredAt} of the oldest pending row; {@code null} when there is none
 */
public record OutboxBacklog(long pending, Instant oldestOccurredAt) {

    public static final OutboxBacklog EMPTY = new OutboxBacklog(0L, null);

    public Duration oldestAge(Instant now) {
        if (oldestOccurredAt == null || oldestOccurredAt.isAfter(now)) {
            return Duration.ZERO;
        }
        return Duration.between(oldestOccurredAt, now);
    }
}
