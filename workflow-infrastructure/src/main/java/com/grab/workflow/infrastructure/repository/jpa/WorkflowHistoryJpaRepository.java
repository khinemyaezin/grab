package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WorkflowHistoryJpaRepository extends JpaRepository<WorkflowHistoryEntity, Long> {

    List<WorkflowHistoryEntity> findByExecutionIdOrderBySequenceNumberAsc(String executionId, Pageable pageable);

    Optional<WorkflowHistoryEntity> findTopByExecutionIdOrderBySequenceNumberDesc(String executionId);
}
