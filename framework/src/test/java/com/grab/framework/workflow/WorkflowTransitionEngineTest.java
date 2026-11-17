package com.grab.framework.workflow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.grab.framework.workflow.WorkflowDsl.choice;
import static com.grab.framework.workflow.WorkflowDsl.parallel;
import static com.grab.framework.workflow.WorkflowDsl.passThrough;
import static com.grab.framework.workflow.WorkflowDsl.sequence;
import static com.grab.framework.workflow.WorkflowDsl.task;
import static com.grab.framework.workflow.WorkflowDsl.workflow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowTransitionEngineTest {

    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");
    private final WorkflowTransitionEngine engine = new WorkflowTransitionEngine(new TestWorkflowFingerprint());

    @Test
    void sequenceMaterializesOutputAndPassesResolvedInput() {
        TaskNode<String, String> first = task("first", String.class, String.class, "catalog.create");
        TaskNode<String, String> second = task("second", String.class, String.class, "catalog.publish");
        WorkflowDefinition<String, String> definition = workflow("sequence", 1, String.class, String.class)
                .root(sequence("root", first, second))
                .outputNode("second")
                .build();

        WorkflowTransition started = engine.start(definition, "execution-1", "scope", 1, "input", BASE);
        WorkflowDispatch firstDispatch = started.dispatches().getFirst();
        assertEquals("input", firstDispatch.input());

        WorkflowTransition afterFirst = engine.accept(
                definition,
                started.state(),
                success("m1", firstDispatch, "created"),
                BASE.plusSeconds(1)
        );
        WorkflowDispatch secondDispatch = afterFirst.dispatches().getFirst();
        assertEquals("created", secondDispatch.input());

        WorkflowTransition completed = engine.accept(
                definition,
                afterFirst.state(),
                success("m2", secondDispatch, "published"),
                BASE.plusSeconds(2)
        );
        assertEquals(ExecutionStatus.COMPLETED, completed.state().status());
        assertEquals("published", completed.state().outputs().get("root"));
        assertEquals("published", completed.state().outputs().get("second"));
        assertEquals("published", completed.state().result());
        assertEquals(NodeExecutionStatus.SUCCEEDED, completed.state().node("root").status());
    }

    @Test
    void passThroughCompletesWithoutDispatchAndProvidesSequenceInput() {
        PassThroughNode<String> skip = passThrough("skip-optional", String.class);
        TaskNode<String, String> finalTask = task("finish", String.class, String.class, "catalog.finish");
        WorkflowDefinition<String, String> definition = workflow("pass-through-sequence", 1, String.class, String.class)
                .root(sequence("root", skip, finalTask))
                .outputNode("finish")
                .build();

        WorkflowTransition started = engine.start(definition, "execution-pass", "scope", 1, "input", BASE);

        assertEquals(1, started.dispatches().size());
        assertEquals("input", started.dispatches().getFirst().input());
        assertEquals(NodeExecutionStatus.SUCCEEDED, started.state().node("skip-optional").status());
        assertEquals("input", started.state().outputs().get("skip-optional"));
    }

    @Test
    void parallelBranchesJoinInDeclarationOrder() {
        TaskNode<String, String> price = task("price", String.class, String.class, "pricing.calculate");
        TaskNode<String, String> stock = task("stock", String.class, String.class, "inventory.reserve");
        ParallelNode parallel = parallel("prepare", price, stock);
        WorkflowDefinition<String, List> definition = workflow("parallel", 1, String.class, List.class)
                .root(parallel)
                .build();

        WorkflowTransition started = engine.start(definition, "execution-2", "scope", 1, "sku", BASE);
        assertEquals(2, started.dispatches().size());
        WorkflowDispatch priceDispatch = started.dispatches().stream().filter(value -> value.nodeId().equals("price")).findFirst().orElseThrow();
        WorkflowDispatch stockDispatch = started.dispatches().stream().filter(value -> value.nodeId().equals("stock")).findFirst().orElseThrow();

        WorkflowTransition afterPrice = engine.accept(definition, started.state(), success("price-message", priceDispatch, "10.00"), BASE.plusSeconds(1));
        WorkflowTransition completed = engine.accept(definition, afterPrice.state(), success("stock-message", stockDispatch, "reserved"), BASE.plusSeconds(2));
        assertEquals(ExecutionStatus.COMPLETED, completed.state().status());
        assertEquals(List.of("10.00", "reserved"), completed.state().outputs().get("prepare"));
    }

    @Test
    void choicesArePermanentAndUnselectedBranchesAreSkipped() {
        TaskNode<String, String> pay = task("pay", String.class, String.class, "pay");
        TaskNode<String, String> reserve = task("reserve", String.class, String.class, "reserve");
        ChoiceNode<String> route = choice("route", String.class, Map.of("pay", pay, "reserve", reserve), "reserve");
        WorkflowDefinition<String, String> definition = workflow("choice", 1, String.class, String.class).root(route).build();

        WorkflowTransition started = engine.start(definition, "execution-3", "scope", 1, "sku", BASE);
        WorkflowTransition selected = started;
        assertEquals("reserve", selected.state().choices().get("route"));
        assertEquals(NodeExecutionStatus.SKIPPED, selected.state().node("pay").status());
        assertEquals("reserve", selected.dispatches().getFirst().nodeId());
    }

    @Test
    void duplicateSuccessIsIdempotentAndStaleFailureDoesNotConsumeRetry() {
        TaskNode<String, String> reserve = task("reserve", String.class, String.class, "reserve");
        WorkflowDefinition<String, String> definition = workflow("retry", 1, String.class, String.class).root(reserve).build();
        WorkflowTransition started = engine.start(definition, "execution-4", "scope", 1, "sku", BASE);
        WorkflowDispatch first = started.dispatches().getFirst();

        WorkflowSignal failed = new WorkflowSignal("failed", "execution-4", "reserve", first.logicalOperationId(),
                first.attemptId(), first.attempt(), first.phase(), WorkflowSignalOutcome.FAILED, null,
                WorkflowFailureClassification.RETRYABLE, null, "temporary", 1, BASE.plusSeconds(1));
        WorkflowTransition retryWait = engine.accept(definition, started.state(), failed, BASE.plusSeconds(1));
        assertTrue(retryWait.dispatches().isEmpty());
        assertEquals(NodeExecutionStatus.RETRY_WAIT, retryWait.state().node("reserve").status());
        WorkflowTransition stale = engine.accept(definition, retryWait.state(), new WorkflowSignal("old-failure", "execution-4", "reserve", first.logicalOperationId(), first.attemptId(), 1, WorkflowPhase.FORWARD, WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.RETRYABLE, null, "temporary", 1, BASE.plusSeconds(1)), BASE.plusSeconds(1));
        assertEquals(SignalDisposition.DUPLICATE, stale.signalDisposition());
        WorkflowTimerIntent timer = retryWait.timers().getFirst();
        WorkflowTransition secondAttempt = engine.onTimer(definition, retryWait.state(), new WorkflowTimer(timer.nodeId(), timer.logicalOperationId(), timer.purpose(), timer.generation(), timer.dueAt(), timer.phase()), timer.dueAt());
        WorkflowDispatch second = secondAttempt.dispatches().getFirst();
        WorkflowTransition completed = engine.accept(definition, secondAttempt.state(), success("success-2", second, "reserved"), BASE.plusSeconds(5));
        WorkflowTransition duplicate = engine.accept(definition, completed.state(), success("success-3", second, "reserved"), BASE.plusSeconds(6));
        assertEquals(SignalDisposition.DUPLICATE, duplicate.signalDisposition());
        assertEquals(ExecutionStatus.COMPLETED, duplicate.state().status());
    }

    @Test
    void cancelWaitsForLateSuccessThenCompensates() {
        TaskNode<String, String> reserve = task("reserve", String.class, String.class, "reserve")
                .withCompensation(CompensationDefinition.of("release", List.of()));
        WorkflowDefinition<String, String> definition = workflow("cancel", 1, String.class, String.class).root(reserve).build();
        WorkflowTransition started = engine.start(definition, "execution-5", "scope", 1, "sku", BASE);
        WorkflowTransition requested = engine.cancel(definition, started.state(), BASE.plusSeconds(1));
        assertEquals(ExecutionStatus.CANCEL_REQUESTED, requested.state().status());
        WorkflowDispatch forward = started.dispatches().getFirst();
        WorkflowTransition compensation = engine.accept(definition, requested.state(), success("late", forward, "reserved"), BASE.plusSeconds(2));
        assertEquals(ExecutionStatus.COMPENSATING, compensation.state().status());
        WorkflowDispatch release = compensation.dispatches().getFirst();
        assertEquals(WorkflowPhase.COMPENSATION, release.phase());
        WorkflowTransition cancelled = engine.accept(
                definition,
                compensation.state(),
                new WorkflowSignal("release-result", "execution-5", release.nodeId(), release.logicalOperationId(), release.attemptId(), release.attempt(), WorkflowPhase.COMPENSATION, WorkflowSignalOutcome.SUCCEEDED, WorkflowUnit.INSTANCE, WorkflowFailureClassification.UNCLASSIFIED, null, null, 1, BASE.plusSeconds(3)),
                BASE.plusSeconds(3)
        );
        assertEquals(ExecutionStatus.CANCELLED, cancelled.state().status());
    }

    @Test
    void terminalCancellationDoesNotRewriteState() {
        TaskNode<String, String> task = task("task", String.class, String.class, "task");
        WorkflowDefinition<String, String> definition = workflow("terminal", 1, String.class, String.class).root(task).build();
        WorkflowTransition started = engine.start(definition, "execution-6", "scope", 1, "sku", BASE);
        WorkflowDispatch dispatch = started.dispatches().getFirst();
        WorkflowTransition completed = engine.accept(definition, started.state(), success("done", dispatch, "ok"), BASE.plusSeconds(1));
        WorkflowTransition cancelled = engine.cancel(definition, completed.state(), BASE.plusSeconds(2));
        assertEquals(ExecutionStatus.COMPLETED, cancelled.state().status());
        assertTrue(cancelled.dispatches().isEmpty());
    }

    @Test
    void delayUsesTimerAndWrongPayloadIsRejected() {
        DelayNode delay = new DelayNode("delay", String.class, Duration.ofSeconds(5));
        WorkflowDefinition<String, String> definition = workflow("delay", 1, String.class, String.class).root(delay).build();
        WorkflowTransition started = engine.start(definition, "execution-7", "scope", 1, "sku", BASE);
        WorkflowTimerIntent intent = started.timers().getFirst();
        WorkflowTransition early = engine.onTimer(definition, started.state(), new WorkflowTimer(intent.nodeId(), intent.logicalOperationId(), intent.purpose(), intent.generation(), intent.dueAt(), intent.phase()), BASE.plusSeconds(4));
        assertEquals(SignalDisposition.IGNORED_STALE, early.signalDisposition());
        WorkflowTransition completed = engine.onTimer(definition, started.state(), new WorkflowTimer(intent.nodeId(), intent.logicalOperationId(), intent.purpose(), intent.generation(), intent.dueAt(), intent.phase()), intent.dueAt());
        assertEquals(ExecutionStatus.COMPLETED, completed.state().status());

        TaskNode<String, String> task = task("typed", String.class, String.class, "typed");
        WorkflowDefinition<String, String> typed = workflow("typed", 1, String.class, String.class).root(task).build();
        WorkflowTransition typedStart = engine.start(typed, "execution-8", "scope", 1, "sku", BASE);
        WorkflowDispatch dispatch = typedStart.dispatches().getFirst();
        assertThrows(IllegalArgumentException.class, () -> engine.accept(typed, typedStart.state(), success("bad", dispatch, 42), BASE.plusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> engine.accept(typed, typedStart.state(), success("null", dispatch, null), BASE.plusSeconds(1)));
    }

    @Test
    void definitionVersionMustMatchEveryTransition() {
        TaskNode<String, String> task = task("task", String.class, String.class, "task");
        WorkflowDefinition<String, String> v1 = workflow("versioned", 1, String.class, String.class).root(task).build();
        WorkflowDefinition<String, String> v2 = workflow("versioned", 2, String.class, String.class).root(task).build();
        WorkflowTransition started = engine.start(v1, "execution-9", "scope", 1, "sku", BASE);
        assertThrows(IllegalArgumentException.class, () -> engine.cancel(v2, started.state(), BASE.plusSeconds(1)));
    }

    @Test
    void rejectsWrongLogicalOperationAndChangedContentWithSameMessageId() {
        TaskNode<String, String> task = task("task", String.class, String.class, "task");
        WorkflowDefinition<String, String> definition = workflow("correlation", 1, String.class, String.class).root(task).build();
        WorkflowTransition started = engine.start(definition, "execution-10", "scope", 1, "sku", BASE);
        WorkflowDispatch dispatch = started.dispatches().getFirst();
        WorkflowSignal wrongOperation = new WorkflowSignal(
                "message-10", "execution-10", "task", "wrong-operation", dispatch.attemptId(), dispatch.attempt(),
                WorkflowPhase.FORWARD, WorkflowSignalOutcome.SUCCEEDED, "ok", WorkflowFailureClassification.UNCLASSIFIED,
                null, null, dispatch.payloadVersion(), BASE
        );
        assertThrows(WorkflowCorrelationException.class, () -> engine.accept(definition, started.state(), wrongOperation, BASE));

        WorkflowSignal accepted = success("message-11", dispatch, "Aa");
        WorkflowTransition completed = engine.accept(definition, started.state(), accepted, BASE);
        WorkflowSignal changed = success("message-11", dispatch, "BB");
        assertThrows(IllegalStateException.class, () -> engine.accept(definition, completed.state(), changed, BASE));
    }

    @Test
    void evaluatesDefaultAndTypedChoiceSelectorOnce() {
        TaskNode<String, String> primary = task("primary", String.class, String.class, "primary");
        TaskNode<String, String> fallback = task("fallback", String.class, String.class, "fallback");
        ChoiceNode<String> choice = new ChoiceNode<>(
                "route",
                String.class,
                Map.of("primary", primary, "fallback", fallback),
                "fallback",
                input -> input.equals("use-primary") ? "primary" : null
        );
        WorkflowDefinition<String, String> definition = workflow("choice-selector", 1, String.class, String.class).root(choice).build();
        WorkflowTransition selected = engine.start(definition, "execution-11", "scope", 1, "use-primary", BASE);
        assertEquals("primary", selected.state().choices().get("route"));
        assertEquals(NodeExecutionStatus.SKIPPED, selected.state().node("fallback").status());
        assertEquals("primary", selected.dispatches().getFirst().nodeId());

        WorkflowDefinition<String, String> defaulted = workflow("choice-default", 1, String.class, String.class)
                .root(new ChoiceNode<>("route", String.class, Map.of("primary", primary, "fallback", fallback), "fallback"))
                .build();
        WorkflowTransition defaultSelection = engine.start(defaulted, "execution-12", "scope", 1, "other", BASE);
        assertEquals("fallback", defaultSelection.state().choices().get("route"));
        assertEquals("fallback", defaultSelection.dispatches().getFirst().nodeId());

        WorkflowDefinition<String, String> selectorDefaulted = workflow("choice-selector-default", 1, String.class, String.class)
                .root(new ChoiceNode<>("route", String.class, Map.of("primary", primary, "fallback", fallback), "fallback", input -> null))
                .build();
        WorkflowTransition selectorDefault = engine.start(selectorDefaulted, "execution-12b", "scope", 1, "other", BASE);
        assertEquals("fallback", selectorDefault.state().choices().get("route"));
    }

    @Test
    void selectorFailureCreatesIncidentWithoutDispatchingBranches() {
        TaskNode<String, String> branch = task("branch", String.class, String.class, "branch");
        ChoiceNode<String> choice = new ChoiceNode<>(
                "route",
                String.class,
                Map.of("branch", branch),
                null,
                input -> { throw new IllegalArgumentException("selector unavailable"); }
        );
        WorkflowDefinition<String, String> definition = workflow("choice-incident", 1, String.class, String.class)
                .root(choice)
                .build();

        WorkflowTransition transition = engine.start(definition, "execution-incident", "scope", 1, "sku", BASE);

        assertEquals(ExecutionStatus.NEEDS_ATTENTION, transition.state().status());
        assertTrue(transition.dispatches().isEmpty());
        assertTrue(transition.state().failureReason().contains("selector unavailable"));
    }

    @Test
    void cancellationAllowsCompensationRetryAndSettlesDelay() {
        TaskNode<String, String> reserve = task("reserve", String.class, String.class, "reserve")
                .withCompensation(new CompensationDefinition("release", List.of(), Duration.ofSeconds(30), RetryPolicy.DEFAULT));
        WorkflowDefinition<String, String> definition = workflow("cancel-retry", 1, String.class, String.class).root(reserve).build();
        WorkflowTransition started = engine.start(definition, "execution-13", "scope", 1, "sku", BASE);
        WorkflowTransition requested = engine.cancel(definition, started.state(), BASE.plusSeconds(1));
        WorkflowDispatch forward = started.dispatches().getFirst();
        WorkflowTransition compensating = engine.accept(definition, requested.state(), success("late-13", forward, "reserved"), BASE.plusSeconds(2));
        WorkflowDispatch release = compensating.dispatches().getFirst();
        WorkflowSignal failedRelease = new WorkflowSignal(
                "release-failed", "execution-13", release.nodeId(), release.logicalOperationId(), release.attemptId(), release.attempt(),
                WorkflowPhase.COMPENSATION, WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.RETRYABLE,
                null, "temporary", 1, BASE.plusSeconds(3)
        );
        WorkflowTransition retryWait = engine.accept(definition, compensating.state(), failedRelease, BASE.plusSeconds(3));
        WorkflowTimerIntent retryTimer = retryWait.timers().stream().filter(timer -> timer.purpose() == TimerPurpose.RETRY).findFirst().orElseThrow();
        WorkflowTransition retryDispatch = engine.onTimer(definition, retryWait.state(), new WorkflowTimer(
                retryTimer.nodeId(), retryTimer.logicalOperationId(), retryTimer.purpose(), retryTimer.generation(), retryTimer.dueAt(), retryTimer.phase()
        ), retryTimer.dueAt());
        assertEquals(1, retryDispatch.dispatches().size());
        assertEquals(WorkflowPhase.COMPENSATION, retryDispatch.dispatches().getFirst().phase());

        DelayNode delay = new DelayNode("delay", String.class, Duration.ofSeconds(5));
        WorkflowDefinition<String, String> delayed = workflow("cancel-delay", 1, String.class, String.class).root(delay).build();
        WorkflowTransition delayedStart = engine.start(delayed, "execution-14", "scope", 1, "sku", BASE);
        WorkflowTransition delayedCancel = engine.cancel(delayed, delayedStart.state(), BASE.plusSeconds(1));
        assertEquals(ExecutionStatus.CANCELLED, delayedCancel.state().status());
        assertEquals(NodeExecutionStatus.SKIPPED, delayedCancel.state().node("delay").status());
        WorkflowTimerIntent delayTimer = delayedStart.timers().getFirst();
        WorkflowTransition settled = engine.onTimer(delayed, delayedCancel.state(), new WorkflowTimer(
                delayTimer.nodeId(), delayTimer.logicalOperationId(), delayTimer.purpose(), delayTimer.generation(), delayTimer.dueAt(), delayTimer.phase()
        ), delayTimer.dueAt());
        assertEquals(ExecutionStatus.CANCELLED, settled.state().status());
    }

    @Test
    void accept_olderFailureRequiresIssuedAttemptIdentity() {
        TaskNode<String, String> reservation = task("reserve", String.class, String.class, "reserve");
        WorkflowDefinition<String, String> definition = workflow("attempts", 1, String.class, String.class)
                .root(reservation).build();
        WorkflowTransition start = engine.start(definition, "attempt-execution", "scope", 1, "sku", BASE);
        WorkflowDispatch first = start.dispatches().getFirst();
        WorkflowSignal failed = new WorkflowSignal("first-failure", "attempt-execution", "reserve",
                first.logicalOperationId(), first.attemptId(), 1, WorkflowPhase.FORWARD, WorkflowSignalOutcome.FAILED,
                null, WorkflowFailureClassification.RETRYABLE, null, "temporary", 1, BASE.plusSeconds(300));
        WorkflowTransition waiting = engine.accept(definition, start.state(), failed, BASE);
        WorkflowTimerIntent intent = waiting.timers().getFirst();
        assertEquals(BASE.plusSeconds(1), intent.dueAt());
        WorkflowTimer timer = new WorkflowTimer(intent.nodeId(), intent.logicalOperationId(), intent.purpose(),
                intent.generation(), intent.dueAt(), intent.phase());
        WorkflowTransition early = engine.onTimer(definition, waiting.state(), timer, BASE);
        assertTrue(early.dispatches().isEmpty());
        WorkflowTransition second = engine.onTimer(definition, waiting.state(), timer, intent.dueAt());
        WorkflowSignal invalid = new WorkflowSignal("invalid", "attempt-execution", "reserve",
                first.logicalOperationId(), "not-an-issued-attempt", 1, WorkflowPhase.FORWARD,
                WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.RETRYABLE,
                null, "temporary", 1, BASE);
        assertThrows(WorkflowCorrelationException.class, () -> engine.accept(definition, second.state(), invalid, BASE));
        WorkflowSignal stale = new WorkflowSignal("stale", "attempt-execution", "reserve",
                first.logicalOperationId(), first.attemptId(), 1, WorkflowPhase.FORWARD,
                WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.RETRYABLE,
                null, "temporary", 1, BASE);
        WorkflowTransition result = engine.accept(definition, second.state(), stale, BASE);
        assertEquals(SignalDisposition.IGNORED_STALE, result.signalDisposition());
        assertEquals(2, result.state().node("reserve").attempts());
        assertTrue(result.dispatches().isEmpty());
    }

    @Test
    void accept_compensationNonRetryableFailure_requiresAttention() {
        TaskNode<String, String> task = task("reserve", String.class, String.class, "reserve")
                .withCompensation(CompensationDefinition.of("release", List.of()));
        WorkflowDefinition<String, String> definition = workflow("compensation", 1, String.class, String.class)
                .root(task)
                .build();
        WorkflowTransition started = engine.start(definition, "execution-comp", "scope", 1, "sku", BASE);
        WorkflowTransition requested = engine.cancel(definition, started.state(), BASE);
        WorkflowSignal late = success("late", started.dispatches().getFirst(), "reserved");
        WorkflowTransition compensating = engine.accept(definition, requested.state(), late, BASE);
        WorkflowDispatch release = compensating.dispatches().getFirst();
        WorkflowSignal failure = new WorkflowSignal("failed", "execution-comp", release.nodeId(),
                release.logicalOperationId(), release.attemptId(), release.attempt(), release.phase(),
                WorkflowSignalOutcome.FAILED, null, WorkflowFailureClassification.NON_RETRYABLE,
                null, "permanent", 1, BASE);

        WorkflowTransition result = engine.accept(definition, compensating.state(), failure, BASE);

        assertEquals(ExecutionStatus.NEEDS_ATTENTION, result.state().status());
        assertTrue(result.timers().isEmpty());
    }

    private static WorkflowSignal success(String messageId, WorkflowDispatch dispatch, Object payload) {
        return new WorkflowSignal(messageId, executionId(dispatch), dispatch.nodeId(), dispatch.logicalOperationId(), dispatch.attemptId(), dispatch.attempt(), dispatch.phase(), WorkflowSignalOutcome.SUCCEEDED, payload, WorkflowFailureClassification.UNCLASSIFIED, null, null, dispatch.payloadVersion(), BASE);
    }

    private static String executionId(WorkflowDispatch dispatch) {
        return dispatch.logicalOperationId().split(":")[0];
    }
}
