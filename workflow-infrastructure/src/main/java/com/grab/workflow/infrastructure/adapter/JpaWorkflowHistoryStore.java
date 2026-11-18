package com.grab.workflow.infrastructure.adapter;

import com.grab.workflow.infrastructure.repository.jpa.WorkflowHistoryJpaRepository;
import com.grab.framework.workflow.WorkflowHistoryEntry;
import com.grab.framework.workflow.WorkflowHistoryStore;
import com.grab.workflow.infrastructure.entity.WorkflowHistoryEntity;

import java.util.Objects;

public final class JpaWorkflowHistoryStore implements WorkflowHistoryStore {

    private final WorkflowHistoryJpaRepository repository;

    public JpaWorkflowHistoryStore(WorkflowHistoryJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public long nextSequence(String executionId) {
        return repository.findTopByExecutionIdOrderBySequenceNumberDesc(executionId)
                .map(entity -> entity.getSequenceNumber() + 1)
                .orElse(1L);
    }

    @Override
    public void append(WorkflowHistoryEntry entry) {
        WorkflowHistoryEntity entity = WorkflowHistoryEntity.empty();
        entity.setExecutionId(entry.executionId());
        entity.setSequenceNumber(entry.sequenceNumber());
        entity.setNodeId(entry.nodeId());
        entity.setEventType(entry.eventType());
        entity.setDetailsJson(entry.detailsJson());
        entity.setOccurredAt(entry.occurredAt());
        repository.save(entity);
    }
}
