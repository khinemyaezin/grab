package com.grab.framework.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.grab.framework.workflow.WorkflowDsl.bind;
import static com.grab.framework.workflow.WorkflowDsl.flow;
import static com.grab.framework.workflow.WorkflowDsl.join;
import static com.grab.framework.workflow.WorkflowDsl.map;
import static com.grab.framework.workflow.WorkflowDsl.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowAuthoringTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final WorkflowIdentity IDENTITY = new WorkflowIdentity("authoring", 2, 2);
    private static final WorkflowOperation<Integer, String> WRITE =
            WorkflowOperation.mutation("owner.write", Integer.class, String.class);

    private final WorkflowTransitionEngine engine = new WorkflowTransitionEngine(new TestWorkflowFingerprint());

    @Test
    void optionalStepJoinsTypedLocalResultsWithoutDispatching() {
        WorkflowStep<String, Integer> size = map("size", String.class, Integer.class, String::length);
        WorkflowStep<String, String> copy = map("copy", String.class, String.class, String::toUpperCase);
        WorkflowStep<String, String> joined = join("join", size, copy, String.class, (length, value) -> length + value);
        WorkflowStep<String, String> optional = when("choice", value -> !value.isEmpty(), joined);
        WorkflowFlow<String, String> start = flow(String.class);
        WorkflowFlow<String, String> steps = start.then(optional);
        WorkflowDefinition<String, String> definition = steps.build(IDENTITY, "root");

        WorkflowTransition run = engine.start(definition, "run", "scope", 2, "box", NOW);
        WorkflowTransition skip = engine.start(definition, "skip", "scope", 2, "", NOW);

        assertEquals("3BOX", run.state().result());
        assertEquals("run", run.state().choices().get("choice"));
        assertEquals("", skip.state().result());
        assertEquals("skip", skip.state().choices().get("choice"));
        assertEquals(NodeExecutionStatus.SKIPPED, skip.state().node("join").status());
        assertTrue(run.dispatches().isEmpty());
        assertTrue(run.timers().isEmpty());
        assertThrows(WorkflowCodecUnavailableException.class,
                () -> engine.start(definition, "wrong", "scope", 1, "box", NOW));
    }

    @Test
    void typedPredicatesPropagateUnexpectedFailures() {
        WorkflowStep<String, String> copy = map("copy", String.class, String.class, input -> input);
        WorkflowStep<String, String> optional = when("choice", input -> {
            throw new IllegalStateException("bug in predicate");
        }, copy);
        WorkflowDefinition<String, String> definition = definition(optional);

        assertThrows(IllegalStateException.class,
                () -> engine.start(definition, "execution", "scope", 2, "sku", NOW));
    }

    @Test
    void retryReusesTheNarrowRequestAndCombinesTheOriginalInput() {
        AtomicInteger mappings = new AtomicInteger();
        WorkflowStep<String, String> step = bind("write", String.class, String.class, WRITE, input -> {
            mappings.incrementAndGet();
            return input.length();
        }, (input, result) -> input + ":" + result);
        WorkflowDefinition<String, String> definition = definition(step);
        WorkflowTransition start = engine.start(definition, "execution", "scope", 2, "sku", NOW);
        WorkflowDispatch dispatch = start.dispatches().getFirst();
        assertEquals(3, dispatch.input());
        assertEquals("sku", start.state().node("write").input());

        WorkflowSignal failure = outcome("retryable", dispatch, WorkflowSignalOutcome.FAILED, null,
                WorkflowFailureClassification.RETRYABLE);
        WorkflowTransition wait = engine.accept(definition, start.state(), failure, NOW);
        WorkflowTimerIntent retry = wait.timers().stream().filter(timer -> timer.purpose() == TimerPurpose.RETRY)
                .findFirst().orElseThrow();
        WorkflowTimer event = new WorkflowTimer(retry.nodeId(), retry.logicalOperationId(),
                retry.purpose(), retry.generation(), retry.dueAt(), retry.phase());
        WorkflowTransition retried = engine.onTimer(definition, wait.state(), event, retry.dueAt());
        WorkflowDispatch second = retried.dispatches().getFirst();
        assertEquals(dispatch.input(), second.input());
        assertEquals(1, mappings.get());

        WorkflowSignal success = outcome("success", second, WorkflowSignalOutcome.SUCCEEDED, "receipt",
                WorkflowFailureClassification.UNCLASSIFIED);
        WorkflowTransition complete = engine.accept(definition, retried.state(), success, NOW);
        assertEquals("sku:receipt", complete.state().result());
        assertEquals("receipt", complete.state().node("write.task").output());
        assertTrue(complete.dispatches().isEmpty());
        WorkflowTransition duplicate = engine.accept(definition, complete.state(), success, NOW);
        assertEquals(SignalDisposition.DUPLICATE, duplicate.signalDisposition());
    }

    @Test
    void expectedCombinerFailurePreservesTheCompletedEffect() {
        CompensationDefinition reverse = CompensationDefinition.of("owner.reverse", List.of());
        Optional<CompensationDefinition> compensation = Optional.of(reverse);
        WorkflowStep<String, String> step = bind("write", String.class, String.class, WRITE, String::length,
                (input, result) -> {
                    throw new WorkflowMappingException("unusable result");
                }, compensation);
        WorkflowDefinition<String, String> definition = definition(step);
        WorkflowTransition start = engine.start(definition, "execution", "scope", 2, "sku", NOW);
        WorkflowDispatch dispatch = start.dispatches().getFirst();
        WorkflowSignal success = outcome("success", dispatch, WorkflowSignalOutcome.SUCCEEDED, "receipt",
                WorkflowFailureClassification.UNCLASSIFIED);
        WorkflowTransition incident = engine.accept(definition, start.state(), success, NOW);

        assertEquals(ExecutionStatus.NEEDS_ATTENTION, incident.state().status());
        assertEquals("unusable result", incident.state().failureReason());
        NodeExecution effect = incident.state().node("write.task");
        assertEquals(NodeExecutionStatus.SUCCEEDED, effect.status());
        assertEquals("receipt", effect.output());
        assertInstanceOf(TaskNode.class, definition.nodes().get("write.task"));
        assertTrue(incident.dispatches().isEmpty());
    }

    @Test
    void cancellationCompensatesTheChildWithoutRunningTheCombiner() {
        CompensationDefinition reverse = CompensationDefinition.of("owner.reverse", List.of());
        Optional<CompensationDefinition> compensation = Optional.of(reverse);
        WorkflowStep<String, String> write = bind("write", String.class, String.class, WRITE, String::length,
                (input, result) -> {
                    throw new AssertionError("Cancelled work should not advance the graph");
                }, compensation);
        WorkflowDefinition<String, String> definition = definition(write);
        WorkflowTransition started = engine.start(definition, "execution", "scope", 2, "sku", NOW);
        WorkflowTransition cancelled = engine.cancel(definition, started.state(), NOW);
        WorkflowDispatch forward = started.dispatches().getFirst();
        WorkflowSignal success = outcome("success", forward, WorkflowSignalOutcome.SUCCEEDED, "receipt",
                WorkflowFailureClassification.UNCLASSIFIED);
        WorkflowTransition compensating = engine.accept(definition, cancelled.state(), success, NOW);

        WorkflowDispatch dispatch = compensating.dispatches().getFirst();
        assertEquals(WorkflowPhase.COMPENSATION, dispatch.phase());
        assertEquals(new CompensationInput<>(3, "receipt"), dispatch.input());
        assertEquals("comp:write.task", dispatch.nodeId());
    }

    @Test
    void readOnlyAndCompensationAreRejectedInEitherOrder() {
        TaskNode<String, String> task = new TaskNode<>("task", String.class, String.class, "owner.operation");
        CompensationDefinition reverse = CompensationDefinition.of("owner.reverse", List.of());

        assertThrows(IllegalArgumentException.class, () -> task.withCompensation(reverse).readOnly());
        assertThrows(IllegalArgumentException.class, () -> task.readOnly().withCompensation(reverse));
    }

    private static WorkflowDefinition<String, String> definition(WorkflowStep<String, String> step) {
        WorkflowFlow<String, String> start = flow(String.class);
        WorkflowFlow<String, String> steps = start.then(step);
        return steps.build(IDENTITY, "root");
    }

    private static WorkflowSignal outcome(String message, WorkflowDispatch dispatch, WorkflowSignalOutcome outcome,
                                          Object result, WorkflowFailureClassification classification) {
        return new WorkflowSignal(message, "execution", dispatch.nodeId(), dispatch.logicalOperationId(),
                dispatch.attemptId(), dispatch.attempt(), dispatch.phase(), outcome, result, classification,
                null, null, dispatch.payloadVersion(), NOW);
    }
}
