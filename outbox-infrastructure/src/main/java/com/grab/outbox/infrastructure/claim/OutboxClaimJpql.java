package com.grab.outbox.infrastructure.claim;

import com.grab.framework.outbox.OutboxStatus;

import java.util.List;

/**
 * JPQL fragments shared by the claim strategies. Field names are those of {@code JpaOutboxEventEntity}.
 */
final class OutboxClaimJpql {

    static final List<OutboxStatus> HOT_CLAIMABLE_STATUSES = List.of(OutboxStatus.NEW, OutboxStatus.FAILED);

    /**
     * Ready-to-retry rows plus stale claims. Parameters: {@code retryableStatuses}, {@code now},
     * {@code processingStatus}, {@code staleBefore}.
     */
    static final String BATCH_CLAIMABLE = """
            (
                (event.status in :retryableStatuses and event.availableAt <= :now)
                or (event.status = :processingStatus and event.claimedAt <= :staleBefore)
            )""";

    /**
     * Hot path: only rows nobody has claimed yet. Parameters: {@code hotStatuses}, {@code now}.
     */
    static final String HOT_CLAIMABLE = """
            (
                event.status in :hotStatuses
                and (event.availableAt is null or event.availableAt <= :now)
            )""";

    private OutboxClaimJpql() {
    }
}
