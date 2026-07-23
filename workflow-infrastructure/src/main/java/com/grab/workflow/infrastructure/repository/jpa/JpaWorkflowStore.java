package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import com.grab.workflow.infrastructure.mapper.WorkflowInstanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaWorkflowStore implements WorkflowStore {

    private final WorkflowInstanceJpaRepository repository;
    private final WorkflowInstanceMapper mapper;

    @Override
    public WorkflowInstance save(WorkflowInstance instance) {
        Objects.requireNonNull(instance, "instance");
        WorkflowInstanceEntity entity = repository.findById(instance.id())
                .orElseGet(WorkflowInstanceEntity::new);
        mapper.toEntity(instance, entity);
        WorkflowInstanceEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WorkflowInstance> findById(String workflowId) {
        return repository.findById(workflowId).map(mapper::toDomain);
    }

    @Override
    public Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey) {
        return repository.findByWorkflowNameAndIdempotencyKey(workflowName, idempotencyKey)
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status) {
        Objects.requireNonNull(workflowName, "workflowName");
        Objects.requireNonNull(status, "status");
        return repository.findByWorkflowNameAndStatus(workflowName, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
