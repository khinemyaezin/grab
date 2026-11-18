package com.grab.workflow.infrastructure.adapter;

import com.grab.workflow.infrastructure.repository.jpa.WorkflowExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowNodeExecutionJpaRepository;
import com.grab.workflow.infrastructure.repository.jpa.WorkflowStartReceiptJpaRepository;
import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowExecutionMetadata;
import com.grab.framework.workflow.WorkflowExecutionState;
import com.grab.framework.workflow.WorkflowExecutionStore;
import com.grab.framework.workflow.WorkflowConcurrencyException;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import com.grab.framework.workflow.WorkflowStoredExecution;
import com.grab.workflow.infrastructure.serialization.WorkflowSnapshotCodec;
import com.grab.workflow.infrastructure.entity.WorkflowExecutionEntity;
import com.grab.workflow.infrastructure.entity.WorkflowNodeExecutionEntity;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.time.Instant;
import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.WorkflowFingerprintUnavailableException;
import java.util.Objects;
import java.util.Optional;

public final class JpaWorkflowExecutionStore implements WorkflowExecutionStore {

    private final WorkflowExecutionJpaRepository executionRepository;
    private final WorkflowNodeExecutionJpaRepository nodeRepository;
    private final WorkflowStartReceiptJpaRepository startReceiptRepository;
    private final WorkflowPayloadCodec payloadCodec;
    private final WorkflowSnapshotCodec snapshotCodec;

    public JpaWorkflowExecutionStore(
            WorkflowExecutionJpaRepository executionRepository,
            WorkflowNodeExecutionJpaRepository nodeRepository,
            WorkflowStartReceiptJpaRepository startReceiptRepository,
            WorkflowPayloadCodec payloadCodec
    ) {
        this.executionRepository = Objects.requireNonNull(executionRepository, "executionRepository");
        this.nodeRepository = Objects.requireNonNull(nodeRepository, "nodeRepository");
        this.startReceiptRepository = Objects.requireNonNull(startReceiptRepository, "startReceiptRepository");
        this.payloadCodec = Objects.requireNonNull(payloadCodec, "payloadCodec");
        this.snapshotCodec = new WorkflowSnapshotCodec(payloadCodec);
    }

    @Override
    public WorkflowStoredExecution create(WorkflowStoredExecution storedExecution) {
        WorkflowExecutionEntity entity = WorkflowExecutionEntity.empty();
        return persist(storedExecution, entity);
    }

    @Override
    public WorkflowStoredExecution save(WorkflowStoredExecution storedExecution) {
        Objects.requireNonNull(storedExecution, "storedExecution");
        WorkflowExecutionState state = storedExecution.state();
        WorkflowExecutionEntity entity = executionRepository.findById(state.executionId())
                .orElseThrow(() -> new WorkflowConcurrencyException(state.executionId(), null));
        Long actualVersion = entity.getVersion();
        if (actualVersion != null && actualVersion != storedExecution.version()) {
            throw new WorkflowConcurrencyException(state.executionId(), null);
        }
        entity.setTransitionRevision(entity.getTransitionRevision() + 1);
        return persist(storedExecution, entity);
    }

    private WorkflowStoredExecution persist(WorkflowStoredExecution storedExecution, WorkflowExecutionEntity entity) {
        WorkflowExecutionState state = storedExecution.state();
        mapExecution(storedExecution, entity);
        try {
            WorkflowExecutionEntity saved = executionRepository.saveAndFlush(entity);
            saveNodes(state, saved.getId());
            return new WorkflowStoredExecution(
                    storedExecution.callerScope(),
                    storedExecution.payloadVersion(),
                    storedExecution.idempotencyKey(),
                    state,
                    saved.getCreatedAt(),
                    saved.getUpdatedAt(),
                    saved.getVersion() == null ? 0L : saved.getVersion()
            );
        } catch (OptimisticLockingFailureException exception) {
            throw new WorkflowConcurrencyException(state.executionId(), exception);
        }
    }

    @Override
    public Optional<WorkflowStoredExecution> find(String executionId, WorkflowDefinition<?, ?> definition) {
        Objects.requireNonNull(executionId, "executionId");
        Objects.requireNonNull(definition, "definition");
        return executionRepository.findForTransition(executionId)
                .map(entity -> toStored(entity, definition));
    }

    @Override
    public boolean recordUnavailable(String executionId, String explanation, Instant now) {
        WorkflowExecutionEntity entity = executionRepository.findById(executionId).orElseThrow();
        if (entity.getStatus() == ExecutionStatus.NEEDS_ATTENTION && explanation.equals(entity.getFailureReason())) {
            return false;
        }
        entity.setStatus(ExecutionStatus.NEEDS_ATTENTION);
        entity.setFailureReason(explanation);
        entity.setUpdatedAt(now);
        try {
            executionRepository.saveAndFlush(entity);
            return true;
        } catch (OptimisticLockingFailureException exception) {
            throw new WorkflowConcurrencyException(executionId, exception);
        }
    }

    @Override
    public Optional<WorkflowExecutionMetadata> findMetadata(String executionId) {
        Objects.requireNonNull(executionId, "executionId");
        return executionRepository.findMetadata(executionId);
    }

    private void mapExecution(WorkflowStoredExecution storedExecution, WorkflowExecutionEntity entity) {
        WorkflowExecutionState state = storedExecution.state();
        entity.setId(state.executionId());
        entity.setDefinitionName(state.workflowName());
        entity.setDefinitionVersion(state.definitionVersion());
        entity.setPayloadVersion(storedExecution.payloadVersion());
        entity.setCallerScope(storedExecution.callerScope());
        entity.setStatus(state.status());
        entity.setInputJson(writeJson(state.input()));
        entity.setResultJson(writeJson(state.result()));
        entity.setOutputsJson(writeJson(state.outputs()));
        entity.setChoicesJson(writeJson(state.choices()));
        entity.setAcceptedSignalsJson(writeJson(state.acceptedSignals()));
        entity.setErrorMessage(state.failureReason());
        entity.setFailureReason(state.failureReason());
        entity.setCancellationRequested(state.cancellationRequested());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(storedExecution.createdAt());
        }
        entity.setUpdatedAt(storedExecution.updatedAt());
    }

    private void saveNodes(WorkflowExecutionState state, String executionId) {
        state.nodes().values().forEach(node -> saveNode(executionId, node));
        state.compensationNodes().values().forEach(node -> saveNode(executionId, node));
        nodeRepository.flush();
    }

    private void saveNode(String executionId, NodeExecution node) {
        WorkflowNodeExecutionEntity entity = nodeRepository.findById(
                        new WorkflowNodeExecutionEntity.Pk(executionId, node.nodeId()))
                .orElseGet(WorkflowNodeExecutionEntity::empty);
        entity.setExecutionId(executionId);
        entity.setNodeId(node.nodeId());
        entity.setBranchId(node.phase().name());
        entity.setPhase(node.phase());
        entity.setLogicalOperationId(node.logicalOperationId());
        entity.setAttemptId(node.attemptId());
        entity.setStatus(node.status());
        entity.setAttempts(node.attempts());
        entity.setInputJson(writeJson(node.input()));
        entity.setOutputJson(writeJson(node.output()));
        entity.setAcceptedOutcomeJson(node.acceptedFingerprint());
        entity.setNodeGeneration(node.generation());
        entity.setErrorMessage(node.error());
        entity.setDeadlineAt(node.deadline());
        entity.setRetryAt(node.retryAt());
        nodeRepository.save(entity);
    }

    private WorkflowStoredExecution toStored(WorkflowExecutionEntity entity, WorkflowDefinition<?, ?> definition) {
        if (entity.isFingerprintRecoveryRequired()) {
            throw new WorkflowFingerprintUnavailableException("Workflow fingerprint recovery required before execution can resume");
        }
        List<WorkflowNodeExecutionEntity> nodeEntities = nodeRepository.findByExecutionIdOrderByNodeIdAsc(entity.getId());
        WorkflowExecutionState state = snapshotCodec.restore(entity, nodeEntities, definition);
        String idempotencyKey = startReceiptRepository.findByExecutionId(entity.getId())
                .map(receipt -> receipt.getIdempotencyKey())
                .orElseThrow(() -> new IllegalStateException("Execution is missing its start receipt: " + entity.getId()));
        return new WorkflowStoredExecution(
                entity.getCallerScope(),
                entity.getPayloadVersion(),
                idempotencyKey,
                state,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion() == null ? 0L : entity.getVersion()
        );
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        return payloadCodec.writeTyped(value);
    }

}
