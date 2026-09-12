package com.grab.store.workflows.engine;

import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.ProcessDefinition;
import com.grab.framework.workflow.StepDefinition;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowOptimisticLockException;
import com.grab.workflow.infrastructure.service.TransactionalWorkflowEngine;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionalWorkflowEngineTest {

    private final CountingTransactionManager transactionManager = new CountingTransactionManager();
    private final ProcessDefinition<String> definition =
            ProcessDefinition.of("test-workflow", String.class, new NoOpStep());

    @Test
    void onSignal_whenConcurrentWriteLoses_shouldRetryInAFreshTransaction() {
        ConflictingEngine delegate = new ConflictingEngine(1);
        WorkflowEngine engine = new TransactionalWorkflowEngine(delegate, transactionManager);

        List<WorkflowInstance> result = engine.onSignal(signal());

        assertThat(result).hasSize(1);
        assertThat(delegate.attempts).isEqualTo(2);
        assertThat(transactionManager.started).isEqualTo(2);
        assertThat(transactionManager.rolledBack).isEqualTo(1);
        assertThat(transactionManager.committed).isEqualTo(1);
    }

    @Test
    void onSignal_whenConflictNeverClears_shouldGiveUpAfterThreeAttempts() {
        ConflictingEngine delegate = new ConflictingEngine(Integer.MAX_VALUE);
        WorkflowEngine engine = new TransactionalWorkflowEngine(delegate, transactionManager);

        assertThatThrownBy(() -> engine.onSignal(signal()))
                .isInstanceOf(WorkflowOptimisticLockException.class);

        assertThat(delegate.attempts).isEqualTo(3);
        assertThat(transactionManager.started).isEqualTo(3);
    }

    @Test
    void start_shouldNotRetry() {
        ConflictingEngine delegate = new ConflictingEngine(1);
        WorkflowEngine engine = new TransactionalWorkflowEngine(delegate, transactionManager);

        assertThatThrownBy(() -> engine.start(definition, "context", null))
                .isInstanceOf(WorkflowOptimisticLockException.class);

        assertThat(transactionManager.started).isEqualTo(1);
    }

    private static InboundSignal signal() {
        return InboundSignal.completion("wf-1", "noop", new TestSignalEvent(), "dedup-1");
    }

    private record TestSignalEvent() implements com.grab.framework.domain.Event {
    }

    private static final class ConflictingEngine implements WorkflowEngine {

        private final int conflictsBeforeSuccess;
        private int attempts;

        private ConflictingEngine(int conflictsBeforeSuccess) {
            this.conflictsBeforeSuccess = conflictsBeforeSuccess;
        }

        private WorkflowInstance run() {
            attempts++;
            if (attempts <= conflictsBeforeSuccess) {
                throw new WorkflowOptimisticLockException("wf-1");
            }
            return WorkflowInstance.start("wf-1", "test-workflow", "wf-1", null);
        }

        @Override
        public <C> WorkflowInstance start(ProcessDefinition<C> definition, C input, String idempotencyKey) {
            return run();
        }

        @Override
        public List<WorkflowInstance> onSignal(InboundSignal signal) {
            return List.of(run());
        }

        @Override
        public Optional<WorkflowInstance> resume(String workflowId) {
            return Optional.of(run());
        }

        @Override
        public void sweep() {
            run();
        }
    }

    private static final class CountingTransactionManager implements PlatformTransactionManager {

        private int started;
        private int committed;
        private int rolledBack;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            started++;
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            committed++;
        }

        @Override
        public void rollback(TransactionStatus status) {
            rolledBack++;
        }
    }

    private static final class NoOpStep implements StepDefinition<String> {

        @Override
        public String name() {
            return "noop";
        }

        @Override
        public String onSignal(String context, InboundSignal signal) {
            return context;
        }

        @Override
        public boolean isComplete(String context) {
            return true;
        }

        @Override
        public List<com.grab.framework.domain.Event> onEnter(String workflowId, String context) {
            return List.of();
        }
    }
}
