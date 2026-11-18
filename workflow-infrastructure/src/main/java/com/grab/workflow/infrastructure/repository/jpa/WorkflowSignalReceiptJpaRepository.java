package com.grab.workflow.infrastructure.repository.jpa;

import com.grab.workflow.infrastructure.entity.WorkflowSignalReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowSignalReceiptJpaRepository extends JpaRepository<WorkflowSignalReceiptEntity, String> {

    Optional<WorkflowSignalReceiptEntity> findFirstByExecutionIdAndLogicalOperationIdAndOutcomeFingerprintAndFingerprintVersionOrderByTransportMessageIdAsc(
            String executionId,
            String logicalOperationId,
            String outcomeFingerprint,
            int fingerprintVersion
    );
}
