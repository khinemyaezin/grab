package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.workflow.infrastructure.config.WorkflowsReadTransactional;
import com.grab.workflow.infrastructure.config.WorkflowsTransactional;
import com.grab.workflow.infrastructure.entity.WorkflowInstanceEntity;
import com.grab.workflow.infrastructure.mapper.WorkflowInstanceMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class JpaWorkflowStore implements WorkflowStore {

    private final WorkflowInstanceJpaRepository repository;
    private final WorkflowInstanceMapper mapper;

    public JpaWorkflowStore(WorkflowInstanceJpaRepository repository, WorkflowInstanceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @WorkflowsTransactional
    public WorkflowInstance save(WorkflowInstance instance) {
        Objects.requireNonNull(instance, "instance");
        WorkflowInstanceEntity entity = repository.findById(instance.id())
                .orElseGet(WorkflowInstanceEntity::new);
        mapper.toEntity(instance, entity);
        WorkflowInstanceEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @WorkflowsReadTransactional
    public Optional<WorkflowInstance> findById(String workflowId) {
        return repository.findById(workflowId).map(mapper::toDomain);
    }

    @Override
    @WorkflowsReadTransactional
    public Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey) {
        return repository.findByWorkflowNameAndIdempotencyKey(workflowName, idempotencyKey)
                .map(mapper::toDomain);
    }
}
