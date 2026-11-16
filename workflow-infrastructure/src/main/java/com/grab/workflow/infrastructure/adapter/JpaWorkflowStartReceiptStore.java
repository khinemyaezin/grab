package com.grab.workflow.infrastructure.adapter;

import com.grab.workflow.infrastructure.repository.jpa.WorkflowStartReceiptJpaRepository;
import com.grab.framework.workflow.WorkflowStartReceipt;
import com.grab.framework.workflow.WorkflowStartReceiptStore;
import com.grab.workflow.infrastructure.entity.WorkflowStartReceiptEntity;
import jakarta.persistence.EntityManager;

import java.util.Objects;
import java.util.Optional;

public final class JpaWorkflowStartReceiptStore implements WorkflowStartReceiptStore {

    private final WorkflowStartReceiptJpaRepository repository;

    private final EntityManager entityManager;

    public JpaWorkflowStartReceiptStore(WorkflowStartReceiptJpaRepository repository, EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public Optional<WorkflowStartReceipt> find(String callerScope, String idempotencyKey) {
        return repository.findByCallerScopeAndIdempotencyKey(callerScope, idempotencyKey)
                .map(this::toReceipt);
    }

    @Override
    public void insert(WorkflowStartReceipt receipt) {
        WorkflowStartReceiptEntity entity = WorkflowStartReceiptEntity.empty();
        entity.setCallerScope(receipt.callerScope());
        entity.setIdempotencyKey(receipt.idempotencyKey());
        entity.setWorkflowName(receipt.definitionName());
        entity.setDefinitionVersion(receipt.definitionVersion());
        entity.setFingerprintVersion(receipt.fingerprintVersion());
        entity.setInputFingerprint(receipt.inputFingerprint());
        entity.setExecutionId(receipt.executionId());
        entity.setCreatedAt(receipt.createdAt());
        WorkflowReceiptInsert.persist(entityManager, entity, "workflow_start_receipt_pkey");
    }

    private WorkflowStartReceipt toReceipt(WorkflowStartReceiptEntity entity) {
        return new WorkflowStartReceipt(
                entity.getCallerScope(),
                entity.getIdempotencyKey(),
                entity.getWorkflowName(),
                entity.getDefinitionVersion(),
                entity.getFingerprintVersion(),
                entity.getInputFingerprint(),
                entity.getExecutionId(),
                entity.getCreatedAt()
        );
    }
}
