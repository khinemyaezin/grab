package com.grab.service.workflow.internal.error;

import com.grab.framework.exception.DomainException;
import com.grab.framework.workflow.WorkflowCodecUnavailableException;
import com.grab.framework.workflow.WorkflowConcurrencyException;
import com.grab.framework.workflow.WorkflowCorrelationException;
import com.grab.framework.workflow.WorkflowDefinitionUnavailableException;
import com.grab.framework.workflow.WorkflowFingerprintUnavailableException;
import com.grab.framework.workflow.WorkflowIdempotencyConflictException;
import com.grab.framework.workflow.WorkflowReceiptUniquenessException;

import java.util.function.Supplier;

public final class WorkflowServiceException extends DomainException {

    private WorkflowServiceException(WorkflowServiceError error, RuntimeException cause) {
        super(error, cause.getMessage(), cause);
    }

    public static <T> T translate(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (WorkflowIdempotencyConflictException exception) {
            throw new WorkflowServiceException(new WorkflowServiceError.Conflict(), exception);
        } catch (WorkflowCorrelationException exception) {
            throw new WorkflowServiceException(new WorkflowServiceError.InvalidCorrelation(), exception);
        } catch (WorkflowDefinitionUnavailableException | WorkflowCodecUnavailableException
                 | WorkflowFingerprintUnavailableException exception) {
            throw new WorkflowServiceException(new WorkflowServiceError.RecoveryRequired(), exception);
        } catch (WorkflowConcurrencyException | WorkflowReceiptUniquenessException exception) {
            throw new WorkflowServiceException(new WorkflowServiceError.ConcurrentUpdate(), exception);
        }
    }
}
