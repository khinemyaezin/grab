package com.grab.store.workflows.internal.service;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.store.shared.sse.WorkflowTerminalUiEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowTerminalLifecycleListenerTest {

    private final List<Object> published = new ArrayList<>();
    private final WorkflowTerminalLifecycleListener listener = new WorkflowTerminalLifecycleListener(
            published::add,
            List.of(new StubAdapter("create-sellable-product"))
    );

    @Test
    void unsupportedWorkflow_shouldNotPublish() {
        listener.onTerminal(
                completed("wf-1", "update-product-variant"),
                new StubContext("actor-1", "merchant-1", true)
        );

        assertThat(published).isEmpty();
    }

    @Test
    void contextMismatch_shouldNotPublish() {
        listener.onTerminal(completed("wf-1", "create-sellable-product"), "not-a-context");

        assertThat(published).isEmpty();
    }

    @Test
    void blankCreatedBy_shouldNotPublish() {
        listener.onTerminal(
                completed("wf-1", "create-sellable-product"),
                new StubContext(" ", "merchant-1", true)
        );

        assertThat(published).isEmpty();
    }

    @Test
    void completed_shouldPublishWithoutPartialFlag() {
        listener.onTerminal(
                completed("wf-1", "create-sellable-product"),
                new StubContext("actor-1", "merchant-1", true)
        );

        assertThat(published).containsExactly(new WorkflowTerminalUiEvent(
                "actor-1",
                "merchant-1",
                "wf-1",
                "create-sellable-product",
                "COMPLETED",
                "idem-1",
                null,
                false
        ));
    }

    @Test
    void failedWithRemainingWrites_shouldPublishPartialFlag() {
        WorkflowInstance instance = WorkflowInstance.restore(
                "wf-1",
                "create-sellable-product",
                "wf-1",
                "idem-1",
                WorkflowStatus.FAILED,
                "create-inventory-item",
                null,
                null,
                "inventory failed",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of()
        );

        listener.onTerminal(instance, new StubContext("actor-1", "merchant-1", true));

        assertThat(published).containsExactly(new WorkflowTerminalUiEvent(
                "actor-1",
                "merchant-1",
                "wf-1",
                "create-sellable-product",
                "FAILED",
                "idem-1",
                "inventory failed",
                true
        ));
    }

    @Test
    void compensatedWithoutRemainingWrites_shouldPublishWithoutPartialFlag() {
        WorkflowInstance instance = WorkflowInstance.restore(
                "wf-1",
                "create-sellable-product",
                "wf-1",
                null,
                WorkflowStatus.COMPENSATED,
                "create-inventory-item",
                null,
                null,
                "inventory failed",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of()
        );

        listener.onTerminal(instance, new StubContext("actor-1", "merchant-1", false));

        assertThat(published).containsExactly(new WorkflowTerminalUiEvent(
                "actor-1",
                "merchant-1",
                "wf-1",
                "create-sellable-product",
                "COMPENSATED",
                null,
                "inventory failed",
                false
        ));
    }

    private static WorkflowInstance completed(String id, String workflowName) {
        WorkflowInstance instance = WorkflowInstance.start(id, workflowName, id, "idem-1");
        instance.markCompleted(null, null);
        return instance;
    }

    private record StubContext(String createdBy, String scopeId, boolean partiallyApplied) {
    }

    private static final class StubAdapter implements WorkflowTerminalContextAdapter<StubContext> {

        private final String workflowName;

        private StubAdapter(String workflowName) {
            this.workflowName = workflowName;
        }

        @Override
        public String workflowName() {
            return workflowName;
        }

        @Override
        public Class<StubContext> contextType() {
            return StubContext.class;
        }

        @Override
        public WorkflowTerminalDetails from(StubContext context) {
            return new WorkflowTerminalDetails(context.createdBy(), context.scopeId(), context.partiallyApplied());
        }
    }
}
