package com.grab.outbox.infrastructure.core.port.out;

import com.grab.framework.outbox.OutboxStatus;
import com.grab.outbox.infrastructure.core.port.in.OutboxBacklog;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface OutboxMaintenanceStore<T, ID> {

    int deletePublishedOlderThan(Instant cutoff, int limit);

    /** Locks up to the requested number of dead rows in ascending ID order. */
    List<T> findDeadForReplay(ID fromId, ID toId, int limit);

    OutboxBacklog backlog(Collection<OutboxStatus> pendingStatuses);

    default OutboxBacklog pendingBacklog() {
        List<OutboxStatus> pending = List.of(OutboxStatus.NEW, OutboxStatus.FAILED, OutboxStatus.PROCESSING);
        return backlog(pending);
    }
}
