package com.grab.framework.workflow.impl;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowOptimisticLockException;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStore;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryWorkflowStore implements WorkflowStore {

    private final Map<String, WorkflowInstance> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyIndex = new ConcurrentHashMap<>();
    private final Map<String, Long> versions = new ConcurrentHashMap<>();
    private final Map<String, CorrelationRecord> correlations = new ConcurrentHashMap<>();
    private final Set<String> signalLog = ConcurrentHashMap.newKeySet();

    @Override
    public WorkflowInstance save(WorkflowInstance instance) {
        Objects.requireNonNull(instance, "instance");
        Long storedVersion = versions.get(instance.id());
        if (storedVersion == null) {
            versions.put(instance.id(), instance.version());
        } else if (storedVersion != instance.version()) {
            throw new WorkflowOptimisticLockException(instance.id());
        } else {
            long next = storedVersion + 1;
            instance.assignVersion(next);
            versions.put(instance.id(), next);
        }
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

    @Override
    public List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status) {
        Objects.requireNonNull(workflowName, "workflowName");
        Objects.requireNonNull(status, "status");
        return byId.values().stream()
                .filter(instance -> instance.workflowName().equals(workflowName))
                .filter(instance -> instance.status() == status)
                .toList();
    }

    @Override
    public Optional<WorkflowInstance> findByCorrelationKey(String workflowName, String correlationKey) {
        CorrelationRecord record = correlations.get(correlationIndexKey(workflowName, correlationKey));
        if (record == null) {
            return Optional.empty();
        }
        return findById(record.workflowId());
    }

    @Override
    public List<WorkflowInstance> findAllByCorrelationKey(String correlationKey) {
        Objects.requireNonNull(correlationKey, "correlationKey");
        return correlations.values().stream()
                .filter(record -> record.correlationKey().equals(correlationKey))
                .map(CorrelationRecord::workflowId)
                .distinct()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void putCorrelation(String workflowName, String correlationKey, String workflowId, String step) {
        Objects.requireNonNull(workflowName, "workflowName");
        Objects.requireNonNull(correlationKey, "correlationKey");
        Objects.requireNonNull(workflowId, "workflowId");
        Objects.requireNonNull(step, "step");
        correlations.put(
                correlationIndexKey(workflowName, correlationKey),
                new CorrelationRecord(correlationKey, workflowId, workflowName, step)
        );
    }

    @Override
    public void removeCorrelationsForWorkflow(String workflowId) {
        correlations.entrySet().removeIf(entry -> entry.getValue().workflowId().equals(workflowId));
    }

    @Override
    public boolean tryRecordSignal(String workflowId, String step, String dedupKey) {
        return signalLog.add(workflowId + "\0" + step + "\0" + dedupKey);
    }

    @Override
    public List<WorkflowInstance> findResumable(
            String workflowName,
            Collection<WorkflowStatus> statuses,
            Instant updatedBefore,
            int limit
    ) {
        Objects.requireNonNull(workflowName, "workflowName");
        Objects.requireNonNull(statuses, "statuses");
        Objects.requireNonNull(updatedBefore, "updatedBefore");
        return byId.values().stream()
                .filter(instance -> instance.workflowName().equals(workflowName))
                .filter(instance -> statuses.contains(instance.status()))
                .filter(instance -> instance.updatedAt().isBefore(updatedBefore))
                .sorted(Comparator.comparing(WorkflowInstance::updatedAt))
                .limit(Math.max(limit, 0))
                .toList();
    }

    private static String idempotencyIndexKey(String workflowName, String idempotencyKey) {
        return workflowName + "\0" + idempotencyKey;
    }

    private static String correlationIndexKey(String workflowName, String correlationKey) {
        return workflowName + "\0" + correlationKey;
    }

    private record CorrelationRecord(String correlationKey, String workflowId, String workflowName, String step) {
    }
}
