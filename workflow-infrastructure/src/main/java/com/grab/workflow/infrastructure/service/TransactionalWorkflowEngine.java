package com.grab.workflow.infrastructure.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.workflow.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class TransactionalWorkflowEngine implements WorkflowEngine {

    private static final Logger log = Loggers.getLogger(TransactionalWorkflowEngine.class);
    private static final int SIGNAL_ATTEMPTS = 3;

    private final WorkflowEngine delegate;
    private final TransactionTemplate transaction;

    public TransactionalWorkflowEngine(WorkflowEngine delegate, PlatformTransactionManager transactionManager) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        Objects.requireNonNull(transactionManager, "transactionManager");
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public <C> WorkflowInstance start(ProcessDefinition<C> definition, C input, String idempotencyKey) {
        return transaction.execute(status -> delegate.start(definition, input, idempotencyKey));
    }

    @Override
    public List<WorkflowInstance> onSignal(InboundSignal signal) {
        return withRetry(
                () -> transaction.execute(status -> delegate.onSignal(signal)),
                "signal " + signal.dedupKey()
        );
    }

    @Override
    public Optional<WorkflowInstance> resume(String workflowId) {
        return withRetry(
                () -> transaction.execute(status -> delegate.resume(workflowId)),
                "resume " + workflowId
        );
    }

    @Override
    public void sweep() {
        transaction.executeWithoutResult(status -> delegate.sweep());
    }

    private <T> T withRetry(Supplier<T> attempt, String description) {
        WorkflowOptimisticLockException lastConflict = null;
        for (int remaining = SIGNAL_ATTEMPTS; remaining > 0; remaining--) {
            try {
                return attempt.get();
            } catch (WorkflowOptimisticLockException conflict) {
                lastConflict = conflict;
                log.debug("Retrying {} after concurrent workflow write", description);
            }
        }
        throw lastConflict;
    }
}
