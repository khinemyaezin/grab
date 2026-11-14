package com.grab.outbox.infrastructure.core.port.out;

import com.grab.framework.outbox.OutboxStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Batch claim criteria. A row is claimable when it is in one of {@code retryableStatuses} and
 * {@code availableAt <= now}, or when it is {@link OutboxStatus#PROCESSING} with {@code claimedAt <= staleBefore}
 * (a claim abandoned by a crashed or stalled worker).
 */
public record OutboxClaimRequest(
        Collection<OutboxStatus> retryableStatuses,
        Instant now,
        Instant staleBefore,
        int limit
) {

    public OutboxClaimRequest {
        retryableStatuses = List.copyOf(Objects.requireNonNull(retryableStatuses, "retryableStatuses"));
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(staleBefore, "staleBefore");
    }
}
