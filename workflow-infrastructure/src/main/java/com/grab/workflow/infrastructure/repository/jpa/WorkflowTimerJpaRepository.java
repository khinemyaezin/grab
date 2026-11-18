package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowTimerStatus;
import com.grab.workflow.infrastructure.entity.WorkflowTimerEntity;
import com.grab.framework.workflow.WorkflowPhase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;

public interface WorkflowTimerJpaRepository extends JpaRepository<WorkflowTimerEntity, Long> {

    boolean existsByExecutionIdAndNodeIdAndNodeGenerationAndPurposeAndPhase(
            String executionId,
            String nodeId,
            long nodeGeneration,
            String purpose,
            WorkflowPhase phase
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<WorkflowTimerEntity> findByStatusAndDueAtLessThanEqualOrderByDueAtAsc(
            WorkflowTimerStatus status,
            Instant dueAt,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<WorkflowTimerEntity> findByStatusAndLeaseExpiresAtLessThanEqualOrderByDueAtAsc(
            WorkflowTimerStatus status,
            Instant leaseExpiresAt,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select timer from WorkflowTimerEntity timer where timer.id = :id")
    java.util.Optional<WorkflowTimerEntity> findByIdForUpdate(@Param("id") Long id);
}
