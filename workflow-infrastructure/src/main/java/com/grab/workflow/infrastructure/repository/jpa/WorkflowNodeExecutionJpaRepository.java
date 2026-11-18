package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowNodeExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowNodeExecutionJpaRepository
        extends JpaRepository<WorkflowNodeExecutionEntity, WorkflowNodeExecutionEntity.Pk> {

    List<WorkflowNodeExecutionEntity> findByExecutionIdOrderByNodeIdAsc(String executionId);
}
