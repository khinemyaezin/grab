package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowCorrelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkflowCorrelationJpaRepository
        extends JpaRepository<WorkflowCorrelationEntity, WorkflowCorrelationEntity.Pk> {

    List<WorkflowCorrelationEntity> findByCorrelationKey(String correlationKey);

    @Modifying
    @Query("delete from WorkflowCorrelationEntity c where c.workflowId = :workflowId")
    void deleteByWorkflowId(@Param("workflowId") String workflowId);
}
