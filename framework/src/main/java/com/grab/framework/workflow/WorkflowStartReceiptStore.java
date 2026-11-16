package com.grab.framework.workflow;

import java.util.Optional;

public interface WorkflowStartReceiptStore {

    Optional<WorkflowStartReceipt> find(String callerScope, String idempotencyKey);

    void insert(WorkflowStartReceipt receipt);
}
