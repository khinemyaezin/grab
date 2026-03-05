package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OutboxStore<T extends OutboxEntry<ID>, ID> {

    void saveAll(Collection<T> events);

    List<T> findBatchForProcessing(
            Collection<OutboxStatus> retryableStatuses,
            OutboxStatus processingStatus,
            LocalDateTime availableAt,
            LocalDateTime staleBefore,
            int batchSize
    );

    Optional<T> findById(ID id);

    void deletePublishedOlderThan(LocalDateTime cutoff);
}
