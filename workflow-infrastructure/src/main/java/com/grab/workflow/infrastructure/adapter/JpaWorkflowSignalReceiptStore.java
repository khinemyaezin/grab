package com.grab.workflow.infrastructure.adapter;

import com.grab.workflow.infrastructure.repository.jpa.WorkflowSignalReceiptJpaRepository;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowSignalOutcome;
import com.grab.framework.workflow.WorkflowSignalReceipt;
import com.grab.framework.workflow.WorkflowSignalReceiptStatus;
import com.grab.framework.workflow.WorkflowSignalReceiptStore;
import com.grab.workflow.infrastructure.entity.WorkflowSignalDisposition;
import com.grab.workflow.infrastructure.entity.WorkflowSignalReceiptEntity;
import jakarta.persistence.EntityManager;

import java.util.Objects;
import java.util.Optional;

public final class JpaWorkflowSignalReceiptStore implements WorkflowSignalReceiptStore {

    private final WorkflowSignalReceiptJpaRepository repository;

    private final EntityManager entityManager;

    public JpaWorkflowSignalReceiptStore(WorkflowSignalReceiptJpaRepository repository, EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public Optional<WorkflowSignalReceipt> findByMessageId(String messageId) {
        return repository.findById(messageId).map(this::toReceipt);
    }

    @Override
    public Optional<WorkflowSignalReceipt> findByFingerprint(
            String executionId,
            String logicalOperationId,
            String outcomeFingerprint,
            int fingerprintVersion
    ) {
        return repository.findFirstByExecutionIdAndLogicalOperationIdAndOutcomeFingerprintAndFingerprintVersionOrderByTransportMessageIdAsc(
                executionId,
                logicalOperationId,
                outcomeFingerprint,
                fingerprintVersion
        ).map(this::toReceipt);
    }

    @Override
    public void insert(WorkflowSignalReceipt receipt) {
        WorkflowSignalReceiptEntity entity = WorkflowSignalReceiptEntity.empty();
        entity.setTransportMessageId(receipt.messageId());
        entity.setExecutionId(receipt.executionId());
        entity.setNodeId(receipt.nodeId());
        entity.setLogicalOperationId(receipt.logicalOperationId());
        entity.setAttemptId(receipt.attemptId());
        entity.setAttempt(receipt.attempt());
        entity.setPhase(receipt.phase());
        entity.setPayloadFingerprint(receipt.payloadFingerprint());
        entity.setOutcomeFingerprint(receipt.outcomeFingerprint());
        entity.setFingerprintVersion(receipt.fingerprintVersion());
        entity.setPayloadVersion(receipt.payloadVersion());
        entity.setOutcome(receipt.outcome());
        entity.setDisposition(WorkflowSignalDisposition.valueOf(receipt.status().name()));
        entity.setReceivedAt(receipt.receivedAt());
        entity.setProcessedAt(receipt.processedAt());
        WorkflowReceiptInsert.persist(entityManager, entity, "workflow_signal_receipt_pkey");
    }

    private WorkflowSignalReceipt toReceipt(WorkflowSignalReceiptEntity entity) {
        return new WorkflowSignalReceipt(
                entity.getTransportMessageId(),
                entity.getExecutionId(),
                entity.getNodeId(),
                entity.getLogicalOperationId(),
                entity.getAttemptId(),
                entity.getAttempt(),
                entity.getPhase() == null ? WorkflowPhase.FORWARD : entity.getPhase(),
                entity.getPayloadFingerprint(),
                entity.getOutcomeFingerprint(),
                entity.getFingerprintVersion(),
                entity.getPayloadVersion(),
                entity.getOutcome() == null ? WorkflowSignalOutcome.UNKNOWN : entity.getOutcome(),
                WorkflowSignalReceiptStatus.valueOf(entity.getDisposition().name()),
                entity.getReceivedAt(),
                entity.getProcessedAt()
        );
    }
}
