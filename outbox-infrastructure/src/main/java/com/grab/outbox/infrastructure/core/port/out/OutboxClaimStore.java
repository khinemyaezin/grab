package com.grab.outbox.infrastructure.core.port.out;

import com.grab.framework.outbox.ClaimedOutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutboxClaimStore<T, ID> {

    List<ClaimedOutboxEvent<ID>> claimBatch(OutboxClaimRequest request);

    Optional<ClaimedOutboxEvent<ID>> claimOne(ID id, Instant now);

    Optional<T> findById(ID id);

    /** Re-reads the row under a lock for fenced outcome writes. */
    Optional<T> findByIdForUpdate(ID id);
}
