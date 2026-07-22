package com.grab.framework.workflow.impl;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStore;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryWorkflowStore implements WorkflowStore {

    private final Map<String, WorkflowInstance> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyIndex = new ConcurrentHashMap<>();

    @Override
    public WorkflowInstance save(WorkflowInstance instance) {
        Objects.requireNonNull(instance, "instance");
        byId.put(instance.id(), instance);
        instance.idempotencyKey().ifPresent(key ->
                idempotencyIndex.put(idempotencyIndexKey(instance.workflowName(), key), instance.id())
        );
        return instance;
    }

    @Override
    public Optional<WorkflowInstance> findById(String workflowId) {
        return Optional.ofNullable(byId.get(workflowId));
    }

    @Override
    public Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey) {
        String workflowId = idempotencyIndex.get(idempotencyIndexKey(workflowName, idempotencyKey));
        if (workflowId == null) {
            return Optional.empty();
        }
        return findById(workflowId);
    }

    private static String idempotencyIndexKey(String workflowName, String idempotencyKey) {
        return workflowName + "\0" + idempotencyKey;
    }
}
