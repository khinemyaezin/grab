package com.grab.framework.workflow;

import java.util.Optional;

public interface WorkflowSignalReceiptStore {

    Optional<WorkflowSignalReceipt> findByMessageId(String messageId);

    Optional<WorkflowSignalReceipt> findByFingerprint(String executionId, String logicalOperationId, String outcomeFingerprint, int fingerprintVersion);

    void insert(WorkflowSignalReceipt receipt);
}
