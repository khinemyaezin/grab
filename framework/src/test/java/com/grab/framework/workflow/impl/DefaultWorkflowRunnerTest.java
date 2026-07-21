package com.grab.framework.workflow.impl;

import com.grab.framework.workflow.WorkflowContext;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowResult;
import com.grab.framework.workflow.WorkflowStep;
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

    private final DefaultWorkflowRunner runner = new DefaultWorkflowRunner();

    @Test
    void run_withAllStepsSucceeding_shouldReturnSuccessAndStoreOutputs() {
        WorkflowDefinition definition = WorkflowDefinition.of(
                "demo-workflow",
                new RecordingStep("step-a", "A"),
                new RecordingStep("step-b", "B")
        );
        WorkflowContext context = new WorkflowContext("corr-1");

        WorkflowResult result = runner.run(definition, context);

        assertTrue(result.succeeded());
        assertInstanceOf(WorkflowResult.Success.class, result);
        assertEquals("demo-workflow", result.workflowName());
        assertEquals("A", context.getRequired("step-a", String.class));
        assertEquals("B", context.getRequired("step-b", String.class));
    }

    @Test
    void run_whenMiddleStepFails_shouldCompensateInReverseOrder() {
        List<String> events = new ArrayList<>();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "compensate-workflow",
                new EventStep("step-a", "A", events),
                new FailingStep("step-b", events),
                new EventStep("step-c", "C", events)
        );
        WorkflowContext context = new WorkflowContext("corr-2");

        WorkflowResult result = runner.run(definition, context);

        assertFalse(result.succeeded());
        WorkflowResult.Failed failed = assertInstanceOf(WorkflowResult.Failed.class, result);
        assertEquals("step-b", failed.failedStep());
        assertTrue(failed.compensationCompleted());
        assertEquals(List.of("execute:step-a", "execute:step-b", "compensate:step-a"), events);
        assertTrue(context.find("step-c", String.class).isEmpty());
    }

    @Test
    void run_whenFirstStepFails_shouldNotCompensate() {
        List<String> events = new ArrayList<>();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "fail-first",
                new FailingStep("step-a", events),
                new EventStep("step-b", "B", events)
        );

        WorkflowResult result = runner.run(definition, new WorkflowContext("corr-3"));

        WorkflowResult.Failed failed = assertInstanceOf(WorkflowResult.Failed.class, result);
        assertEquals("step-a", failed.failedStep());
        assertTrue(failed.compensationCompleted());
        assertEquals(List.of("execute:step-a"), events);
    }

    @Test
    void run_whenCompensationFails_shouldMarkCompensationIncomplete() {
        List<String> events = new ArrayList<>();
        WorkflowDefinition definition = WorkflowDefinition.of(
                "bad-compensate",
                new BrokenCompensateStep("step-a", "A", events),
                new FailingStep("step-b", events)
        );

        WorkflowResult result = runner.run(definition, new WorkflowContext("corr-4"));

        WorkflowResult.Failed failed = assertInstanceOf(WorkflowResult.Failed.class, result);
        assertEquals("step-b", failed.failedStep());
        assertFalse(failed.compensationCompleted());
        assertEquals(List.of("execute:step-a", "execute:step-b", "compensate:step-a"), events);
    }

    @Test
    void run_withEmptyDefinition_shouldReject() {
        assertThrows(IllegalArgumentException.class, () -> WorkflowDefinition.of("empty"));
    }

    @Test
    void run_shouldInvokeEachStepOnceOnSuccess() {
        AtomicInteger executions = new AtomicInteger();
        WorkflowStep countingStep = new WorkflowStep() {
            @Override
            public String name() {
                return "count";
            }

            @Override
            public Object execute(WorkflowContext context) {
                return executions.incrementAndGet();
            }
        };

        WorkflowResult result = runner.run(
                WorkflowDefinition.of("once", countingStep),
                new WorkflowContext("corr-5")
        );

        assertTrue(result.succeeded());
        assertEquals(1, executions.get());
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
