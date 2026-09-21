package com.grab.store.shared.workflow;

import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.domain.Event;
import com.grab.framework.workflow.WorkflowStepRunner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowStepRunnerTest {

    private final FakeModuleOutbox outbox = new FakeModuleOutbox();
    private final WorkflowStepRunner emitter = new CatalogWorkflowStepRunner(
            outbox.producer(),
            outbox.transactionManager()
    );

    @Test
    void completionSignal_shouldCommitWithTheStep() {
        emitter.runStep("wf-1", () -> List.of(new TestEvent("completed")), exception -> List.of());

        assertThat(outbox.committed()).containsExactly(new TestEvent("completed"));
    }

    @Test
    void failureSignal_shouldCommitEvenThoughTheStepRolledBack() {
        emitter.runStep(
                "wf-1",
                () -> {
                    throw new IllegalStateException("boom");
                },
                exception -> List.of(new TestEvent(exception.getMessage()))
        );

        assertThat(outbox.committed()).containsExactly(new TestEvent("boom"));
    }

    @Test
    void completionSignal_shouldBeDiscardedWhenTheStepFailsAfterProducingIt() {
        emitter.runStep(
                "wf-1",
                () -> {
                    outbox.producer().produce("workflow", "wf-1", List.of(new TestEvent("completed")));
                    throw new IllegalStateException("boom");
                },
                exception -> List.of()
        );

        assertThat(outbox.committed()).isEmpty();
    }

    private record TestEvent(String name) implements Event {
    }
}
