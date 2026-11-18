package com.grab.framework.workflow;

/**
 * A durable receipt was inserted concurrently by another workflow worker.
 * Retrying this exception is safe because the receipt is the idempotency
 * boundary and the operation is re-read after the failed transaction.
 */
public final class WorkflowReceiptUniquenessException extends RuntimeException {

    public WorkflowReceiptUniquenessException(String message, Throwable cause) {
        super(message, cause);
    }
}
