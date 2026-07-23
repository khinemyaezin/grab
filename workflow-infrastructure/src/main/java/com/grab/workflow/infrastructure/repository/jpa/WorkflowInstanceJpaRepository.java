package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.framework.workflow.WorkflowStatus;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceJpaRepository extends JpaRepository<WorkflowInstanceEntity, String> {

    Optional<WorkflowInstanceEntity> findByWorkflowNameAndIdempotencyKey(String workflowName, String idempotencyKey);

    List<WorkflowInstanceEntity> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status);
}
