package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.outbox.CatalogOutboxEvent;
import com.grab.framework.outbox.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CatalogOutboxEventJpaRepository extends JpaRepository<CatalogOutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from CatalogOutboxEvent event
            where
                (event.status in :retryableStatuses and event.availableAt <= :availableAt)
                or (event.status = :processingStatus and event.claimedAt <= :staleBefore)
            order by event.occurredAt asc
            """)
    List<CatalogOutboxEvent> findBatchForProcessing(
            @Param("retryableStatuses") Collection<OutboxStatus> retryableStatuses,
            @Param("processingStatus") OutboxStatus processingStatus,
            @Param("availableAt") LocalDateTime availableAt,
            @Param("staleBefore") LocalDateTime staleBefore,
            Pageable pageable
    );
}
