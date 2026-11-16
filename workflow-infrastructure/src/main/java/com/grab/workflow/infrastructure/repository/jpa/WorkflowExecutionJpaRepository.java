package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.WorkflowExecutionMetadata;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.grab.workflow.infrastructure.entity.WorkflowExecutionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkflowExecutionJpaRepository extends JpaRepository<WorkflowExecutionEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select execution from WorkflowExecutionEntity execution where execution.id = :id")
    Optional<WorkflowExecutionEntity> findForTransition(@Param("id") String executionId);

    @Query("""
            select new com.grab.framework.workflow.WorkflowExecutionMetadata(
                execution.id, execution.definitionName, execution.definitionVersion, execution.payloadVersion,
                execution.callerScope, execution.status, execution.cancellationRequested, execution.createdAt,
                execution.updatedAt)
            from WorkflowExecutionEntity execution
            where execution.id = :id
            """)
    Optional<WorkflowExecutionMetadata> findMetadata(@Param("id") String executionId);

    List<WorkflowExecutionEntity> findByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            Collection<ExecutionStatus> statuses,
            Instant updatedAt,
            Pageable pageable
    );

    List<WorkflowExecutionEntity> findByCallerScopeAndStatusInOrderByUpdatedAtAscIdAsc(
            String callerScope,
            Collection<ExecutionStatus> statuses,
            Pageable pageable
    );

    Optional<WorkflowExecutionEntity> findByDefinitionNameAndDefinitionVersionAndCallerScope(
            String definitionName,
            int definitionVersion,
            String callerScope
    );
}
