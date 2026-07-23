package com.grab.framework.workflow.impl;

import com.grab.framework.workflow.WorkflowCheckpoint;
import com.grab.framework.workflow.WorkflowContext;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowResult;
import com.grab.framework.workflow.WorkflowRunRequest;
import com.grab.framework.workflow.WorkflowStatus;
import com.grab.framework.workflow.WorkflowStep;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWorkflowRunnerTest {

    private final InMemoryWorkflowStore workflowStore = new InMemoryWorkflowStore();
    private final DefaultWorkflowRunner runner = new DefaultWorkflowRunner(workflowStore);

    @Test
    void run_withAllStepsSucceeding_shouldCheckpointAndComplete() {
        WorkflowDefinition definition = WorkflowDefinition.of(
                "demo-process",
                new RecordingStep("step-a", "A"),
                new RecordingStep("step-b", "B")
        );
        WorkflowContext context = new WorkflowContext("corr-1");

        WorkflowResult result = runner.run(definition, context, WorkflowRunRequest.of("proc-1"));

        assertTrue(result.succeeded());
        assertInstanceOf(WorkflowResult.Success.class, result);
        assertEquals("A", context.getRequired("step-a", String.class));
        assertEquals("B", context.getRequired("step-b", String.class));

        WorkflowInstance stored = workflowStore.findById("proc-1").orElseThrow();
        assertEquals(WorkflowStatus.COMPLETED, stored.status());
        assertEquals(2, stored.checkpoints().size());
        assertTrue(stored.contextJson().isPresent());
        assertTrue(stored.checkpointJson().isPresent());
    }

    @Test
    void run_whenMiddleStepFails_shouldCompensateAndMarkCompensated() {
        List<String> events = new ArrayList<>();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "compensate-process",
                new EventStep("step-a", "A", events),
                new FailingStep("step-b", events),
                new EventStep("step-c", "C", events)
        );

        WorkflowResult result = runner.run(
                definition,
                new WorkflowContext("corr-2"),
                WorkflowRunRequest.of("proc-2")
        );

        WorkflowResult.Failed failed = assertInstanceOf(WorkflowResult.Failed.class, result);
        assertEquals("step-b", failed.failedStep());
        assertTrue(failed.compensationCompleted());
        assertEquals(List.of("execute:step-a", "execute:step-b", "compensate:step-a"), events);

        WorkflowInstance stored = workflowStore.findById("proc-2").orElseThrow();
        assertEquals(WorkflowStatus.COMPENSATED, stored.status());
    }

    @Test
    void run_whenCompensationFails_shouldMarkFailed() {
        List<String> events = new ArrayList<>();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "bad-compensate",
                new BrokenCompensateStep("step-a", "A", events),
                new FailingStep("step-b", events)
        );

        WorkflowResult result = runner.run(
                definition,
                new WorkflowContext("corr-3"),
                WorkflowRunRequest.of("proc-3")
        );

        WorkflowResult.Failed failed = assertInstanceOf(WorkflowResult.Failed.class, result);
        assertFalse(failed.compensationCompleted());

        WorkflowInstance stored = workflowStore.findById("proc-3").orElseThrow();
        assertEquals(WorkflowStatus.FAILED, stored.status());
    }

    @Test
    void run_withCompletedIdempotencyKey_shouldReturnStoredSuccess() {
        WorkflowDefinition definition = WorkflowDefinition.of(
                "idempotent-process",
                new RecordingStep("step-a", "A")
        );
        WorkflowContext firstContext = new WorkflowContext("corr-4");
        runner.run(definition, firstContext, WorkflowRunRequest.of("proc-4", "idem-1"));

        AtomicInteger executions = new AtomicInteger();
        WorkflowStep countingStep = new WorkflowStep() {
            @Override
            public String name() {
                return "step-a";
            }

            @Override
            public Object execute(WorkflowContext context) {
                return executions.incrementAndGet();
            }
        };
        WorkflowDefinition secondDefinition = WorkflowDefinition.of("idempotent-process", countingStep);
        WorkflowContext secondContext = new WorkflowContext("corr-4b");

        WorkflowResult result = runner.run(
                secondDefinition,
                secondContext,
                WorkflowRunRequest.of("proc-4b", "idem-1")
        );

        assertTrue(result.succeeded());
        assertEquals(0, executions.get());
        assertEquals("A", secondContext.getRequired("step-a", String.class));
    }

    @Test
    void run_withEmptyDefinition_shouldReject() {
        assertThrows(IllegalArgumentException.class, () -> WorkflowDefinition.of("empty"));
    }

    @Test
    void resume_fromCheckpoint_shouldSkipCompletedStepsAndContinue() {
        List<String> events = new ArrayList<>();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "resume-process",
                new EventStep("step-a", "A", events),
                new EventStep("step-b", "B", events),
                new EventStep("step-c", "C", events)
        );

        WorkflowInstance crashed = WorkflowInstance.start("wf-resume-1", "resume-process", "corr-r1", null);
        WorkflowContext seed = new WorkflowContext("corr-r1");
        seed.put("step-a", "A");
        WorkflowPayloadCodec codec = new WorkflowPayloadCodec();
        String contextJson = codec.writeContext(seed);
        String checkpointJson = codec.writeCheckpoints(List.of(new WorkflowCheckpoint("step-a", "A")));
        crashed.recordCheckpoint("step-a", "A", contextJson, checkpointJson);
        workflowStore.save(crashed);

        WorkflowResult result = runner.resume(definition, "wf-resume-1");

        assertTrue(result.succeeded());
        assertEquals(List.of("execute:step-b", "execute:step-c"), events);
        assertEquals("A", result.context().getRequired("step-a", String.class));
        assertEquals("B", result.context().getRequired("step-b", String.class));
        assertEquals("C", result.context().getRequired("step-c", String.class));

        WorkflowInstance stored = workflowStore.findById("wf-resume-1").orElseThrow();
        assertEquals(WorkflowStatus.COMPLETED, stored.status());
        assertEquals(3, stored.checkpoints().size());
    }

    @Test
    void run_withSameWorkflowIdWhileRunning_shouldResumeFromCheckpoint() {
        AtomicInteger stepAExecutions = new AtomicInteger();
        AtomicInteger stepBExecutions = new AtomicInteger();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "rerun-process",
                countingStep("step-a", "A", stepAExecutions),
                countingStep("step-b", "B", stepBExecutions)
        );

        WorkflowInstance crashed = WorkflowInstance.start("wf-resume-2", "rerun-process", "corr-r2", "idem-r2");
        WorkflowContext seed = new WorkflowContext("corr-r2");
        seed.put("step-a", "A");
        WorkflowPayloadCodec codec = new WorkflowPayloadCodec();
        crashed.recordCheckpoint(
                "step-a",
                "A",
                codec.writeContext(seed),
                codec.writeCheckpoints(List.of(new WorkflowCheckpoint("step-a", "A")))
        );
        workflowStore.save(crashed);

        WorkflowResult result = runner.run(
                definition,
                new WorkflowContext("corr-r2"),
                WorkflowRunRequest.of("wf-resume-2", "idem-r2")
        );

        assertTrue(result.succeeded());
        assertEquals(0, stepAExecutions.get());
        assertEquals(1, stepBExecutions.get());
    }

    @Test
    void resume_whenAllStepsCheckpointedButStillRunning_shouldMarkCompletedWithoutReexecuting() {
        AtomicInteger executions = new AtomicInteger();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "almost-done",
                countingStep("step-a", "A", executions)
        );

        WorkflowInstance crashed = WorkflowInstance.start("wf-resume-3", "almost-done", "corr-r3", null);
        WorkflowContext seed = new WorkflowContext("corr-r3");
        seed.put("step-a", "A");
        WorkflowPayloadCodec codec = new WorkflowPayloadCodec();
        crashed.recordCheckpoint(
                "step-a",
                "A",
                codec.writeContext(seed),
                codec.writeCheckpoints(List.of(new WorkflowCheckpoint("step-a", "A")))
        );
        workflowStore.save(crashed);

        WorkflowResult result = runner.resume(definition, "wf-resume-3");

        assertTrue(result.succeeded());
        assertEquals(0, executions.get());
        assertEquals(WorkflowStatus.COMPLETED, workflowStore.findById("wf-resume-3").orElseThrow().status());
    }

    @Test
    void resume_whenCompensated_shouldReject() {
        WorkflowDefinition definition = WorkflowDefinition.of(
                "dead-process",
                new RecordingStep("step-a", "A")
        );
        runner.run(
                WorkflowDefinition.of("dead-process", new FailingStep("step-a", new ArrayList<>())),
                new WorkflowContext("corr-dead"),
                WorkflowRunRequest.of("wf-dead")
        );

        assertThrows(IllegalStateException.class, () -> runner.resume(definition, "wf-dead"));
    }

    private static WorkflowStep countingStep(String name, String output, AtomicInteger counter) {
        return new WorkflowStep() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Object execute(WorkflowContext context) {
                counter.incrementAndGet();
                return output;
            }
        };
    }

    private static final class RecordingStep implements WorkflowStep {
        private final String name;
        private final String output;

        private RecordingStep(String name, String output) {
            this.name = name;
            this.output = output;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object execute(WorkflowContext context) {
            return output;
        }
    }

    private static final class EventStep implements WorkflowStep {
        private final String name;
        private final String output;
        private final List<String> events;

        private EventStep(String name, String output, List<String> events) {
            this.name = name;
            this.output = output;
            this.events = events;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object execute(WorkflowContext context) {
            events.add("execute:" + name);
            return output;
        }

        @Override
        public void compensate(WorkflowContext context, Object stepOutput) {
            events.add("compensate:" + name);
        }
    }

    private static final class FailingStep implements WorkflowStep {
        private final String name;
        private final List<String> events;

        private FailingStep(String name, List<String> events) {
            this.name = name;
            this.events = events;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object execute(WorkflowContext context) {
            events.add("execute:" + name);
            throw new IllegalStateException("boom:" + name);
        }
    }

    private static final class BrokenCompensateStep implements WorkflowStep {
        private final String name;
        private final String output;
        private final List<String> events;

        private BrokenCompensateStep(String name, String output, List<String> events) {
            this.name = name;
            this.output = output;
            this.events = events;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object execute(WorkflowContext context) {
            events.add("execute:" + name);
            return output;
        }

        @Override
        public void compensate(WorkflowContext context, Object stepOutput) {
            events.add("compensate:" + name);
            throw new IllegalStateException("compensate-failed:" + name);
        }
    }
}
