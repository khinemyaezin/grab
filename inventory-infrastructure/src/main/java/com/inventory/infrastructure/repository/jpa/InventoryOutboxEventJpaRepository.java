package com.inventory.infrastructure.repository.jpa;

import com.grab.framework.outbox.OutboxStatus;
import com.inventory.infrastructure.outbox.InventoryOutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface InventoryOutboxEventJpaRepository extends JpaRepository<InventoryOutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from InventoryOutboxEvent event
            where
                (event.status in :retryableStatuses and event.availableAt <= :availableAt)
                or (event.status = :processingStatus and event.claimedAt <= :staleBefore)
            order by event.occurredAt asc
            """)
    List<InventoryOutboxEvent> findBatchForProcessing(
            @Param("retryableStatuses") Collection<OutboxStatus> retryableStatuses,
            @Param("processingStatus") OutboxStatus processingStatus,
            @Param("availableAt") LocalDateTime availableAt,
            @Param("staleBefore") LocalDateTime staleBefore,
            Pageable pageable
    );
}
