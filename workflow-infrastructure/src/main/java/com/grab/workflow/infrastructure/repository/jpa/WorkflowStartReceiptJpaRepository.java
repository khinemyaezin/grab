package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowStartReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowStartReceiptJpaRepository
        extends JpaRepository<WorkflowStartReceiptEntity, WorkflowStartReceiptEntity.Pk> {

    Optional<WorkflowStartReceiptEntity> findByCallerScopeAndIdempotencyKey(String callerScope, String idempotencyKey);

    Optional<WorkflowStartReceiptEntity> findByExecutionId(String executionId);
}
