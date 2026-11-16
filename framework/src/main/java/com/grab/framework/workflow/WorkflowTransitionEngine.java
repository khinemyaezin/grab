package com.grab.framework.workflow;

import java.time.Instant;
import com.grab.framework.workflow.internal.ExecutionUpdate;
import com.grab.framework.workflow.internal.CompositeProgression;
import com.grab.framework.workflow.internal.CompositeProgression.ChoiceSelectionException;
import com.grab.framework.workflow.internal.ForwardDispatchPolicy;
import com.grab.framework.workflow.internal.CompensationPolicy;
import com.grab.framework.workflow.internal.SignalCorrelation;
import com.grab.framework.workflow.internal.RetryScheduling;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class WorkflowTransitionEngine {

    private static final int DEFAULT_PARALLEL_LIMIT = 8;
    private final WorkflowFingerprint fingerprints;
    private final ForwardDispatchPolicy forwardDispatch;
    private final CompensationPolicy compensation;
    private final CompensationPlanner compensationPlanner = new CompensationPlanner();

    public WorkflowTransitionEngine(WorkflowFingerprint fingerprints) {
        this(fingerprints, DEFAULT_PARALLEL_LIMIT);
    }

    public WorkflowTransitionEngine(WorkflowFingerprint fingerprints, int parallelLimit) {
        this.fingerprints = Objects.requireNonNull(fingerprints, "fingerprints");
        if (parallelLimit < 1) {
            throw new IllegalArgumentException("parallelLimit must be positive");
        }
        this.forwardDispatch = new ForwardDispatchPolicy(parallelLimit);
        this.compensation = new CompensationPolicy(parallelLimit, compensationPlanner);
    }

    public <I, O> WorkflowTransition start(
            WorkflowDefinition<I, O> definition,
            String executionId,
            String callerScope,
            int payloadVersion,
            I input,
            Instant now
    ) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(now, "now");
        if (!definition.inputType().isInstance(input)) {
            throw new IllegalArgumentException("input does not match workflow input type");
        }
        Map<String, NodeExecution> nodes = new LinkedHashMap<>();
        definition.nodes().keySet().forEach(nodeId -> nodes.put(nodeId, NodeExecution.pending(nodeId)));
        WorkflowExecutionState state = new WorkflowExecutionState(
                executionId,
                definition.name(),
                definition.version(),
                callerScope,
                payloadVersion,
                ExecutionStatus.RUNNING,
                input,
                nodes,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                false,
                null,
                null
        );
        return advance(definition, state, now, SignalDisposition.ACCEPTED);
    }

    public WorkflowTransition accept(
            WorkflowDefinition<?, ?> definition,
            WorkflowExecutionState state,
            WorkflowSignal signal,
            Instant now
    ) {
        verifyDefinition(definition, state);
        Objects.requireNonNull(signal, "signal");
        Objects.requireNonNull(now, "now");
        if (!state.executionId().equals(signal.executionId())) {
            throw new WorkflowCorrelationException("Signal execution ID does not match state");
        }
        if (signal.payloadVersion() != state.payloadVersion()) {
            throw new WorkflowCorrelationException("Signal payload version does not match state");
        }
        WorkflowFingerprintValue transport = fingerprints.transport(signal);
        WorkflowFingerprintValue outcome = fingerprints.signal(signal);
        String transportFingerprint = transport.digest();
        String outcomeFingerprint = outcome.digest();
        Map<String, String> acceptedSignals = new LinkedHashMap<>(state.acceptedSignals());
        String receivedFingerprint = acceptedSignals.get(signal.messageId());
        if (receivedFingerprint != null) {
            if (!receivedFingerprint.equals(transportFingerprint)) {
                throw new WorkflowIdempotencyConflictException("Signal message ID was reused with different content");
            }
            return unchanged(state, SignalDisposition.DUPLICATE);
        }
        Map<String, NodeExecution> nodes = new LinkedHashMap<>(state.nodes());
        Map<String, NodeExecution> compensationNodes = new LinkedHashMap<>(state.compensationNodes());
        NodeExecution node = SignalCorrelation.resolve(signal, nodes, compensationNodes);
        boolean staleAttempt = signal.attempt() < node.attempts();
        if (staleAttempt && signal.outcome() == WorkflowSignalOutcome.FAILED) {
            acceptedSignals.put(signal.messageId(), transportFingerprint);
            return new WorkflowTransition(ExecutionUpdate.from(state)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .acceptedSignals(acceptedSignals)
                    .build(), List.of(), List.of(), SignalDisposition.IGNORED_STALE);
        }
        if (node.acceptedFingerprint() != null) {
            if (node.acceptedFingerprint().equals(outcomeFingerprint)) {
                acceptedSignals.put(signal.messageId(), transportFingerprint);
                return new WorkflowTransition(ExecutionUpdate.from(state)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .acceptedSignals(acceptedSignals)
                    .build(), List.of(), List.of(), SignalDisposition.DUPLICATE);
            }
            throw new WorkflowCorrelationException("Contradictory outcome for logical operation");
        }
        if (node.status() == NodeExecutionStatus.SUCCEEDED || node.status() == NodeExecutionStatus.COMPENSATED) {
            throw new WorkflowCorrelationException("Contradictory outcome for completed operation");
        }
        if (staleAttempt) {
            throw new WorkflowCorrelationException("Signal attempt is stale");
        }
        if (signal.attempt() > node.attempts() || !Objects.equals(signal.attemptId(), node.attemptId()) || node.status() != NodeExecutionStatus.DISPATCHED) {
            throw new WorkflowCorrelationException("Signal does not match the active attempt");
        }
        if (signal.outcome() == WorkflowSignalOutcome.SUCCEEDED) {
            if (signal.payload() == null) {
                throw new IllegalArgumentException("Successful workflow outcomes require a non-null payload");
            }
            if (signal.phase() == WorkflowPhase.FORWARD) {
                WorkflowNode definitionNode = definition.nodes().get(signal.nodeId());
                validateOutput(definitionNode, signal.payload());
            } else if (signal.payload() != WorkflowUnit.INSTANCE) {
                throw new WorkflowCorrelationException("Compensation acknowledgement requires the unit payload");
            }
        }
        acceptedSignals.put(signal.messageId(), transportFingerprint);
        Map<String, Object> outputs = new LinkedHashMap<>(state.outputs());
        if (signal.outcome() == WorkflowSignalOutcome.SUCCEEDED) {
            NodeExecution updated = node.succeed(signal.payload(), outcomeFingerprint);
            if (signal.phase() == WorkflowPhase.COMPENSATION) {
                compensationNodes.put(signal.nodeId(), updated);
            } else {
                nodes.put(signal.nodeId(), updated);
                outputs.put(signal.nodeId(), signal.payload());
            }
        } else if (signal.outcome() == WorkflowSignalOutcome.UNKNOWN) {
            NodeExecution updated = node.unknown(signal.error());
            if (signal.phase() == WorkflowPhase.COMPENSATION) {
                compensationNodes.put(signal.nodeId(), updated);
            } else {
                nodes.put(signal.nodeId(), updated);
            }
            return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .outputs(outputs)
                    .acceptedSignals(acceptedSignals)
                    .failureReason(signal.error())
                    .build(), List.of(), List.of(), SignalDisposition.ACCEPTED);
        } else {
            if (signal.phase() == WorkflowPhase.COMPENSATION) {
                String sourceId = signal.nodeId().substring("comp:".length());
                TaskNode<?, ?> source = (TaskNode<?, ?>) requireNode(definition, sourceId);
                CompensationDefinition compensationDefinition = source.compensation().orElseThrow();
                RetryScheduling.Decision decision = RetryScheduling.afterFailure(
                        node, compensationDefinition.retryPolicy(), signal, outcomeFingerprint, now);
                compensationNodes.put(signal.nodeId(), decision.state());
                WorkflowTimerIntent retryTimer = decision.timer();
                WorkflowExecutionState updated = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.COMPENSATING)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .outputs(outputs)
                    .acceptedSignals(acceptedSignals)
                    .build();
                WorkflowTransition transition = advance(definition, updated, now, SignalDisposition.ACCEPTED);
                return retryTimer == null ? transition : withTimer(transition, retryTimer);
            } else {
                WorkflowNode workflowNode = requireNode(definition, signal.nodeId());
                RetryPolicy policy = retryPolicy(workflowNode);
                RetryScheduling.Decision decision = RetryScheduling.afterFailure(node, policy, signal, outcomeFingerprint, now);
                nodes.put(signal.nodeId(), decision.state());
                if (decision.timer() != null) {
                    WorkflowExecutionState updated = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.WAITING)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .outputs(outputs)
                    .acceptedSignals(acceptedSignals)
                    .failureReason(signal.error())
                    .build();
                    WorkflowTransition advanced = advance(definition, updated, now, SignalDisposition.ACCEPTED);
                    return withTimer(advanced, decision.timer());
                }
            }
        }
        WorkflowExecutionState updated = ExecutionUpdate.from(state)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .outputs(outputs)
                    .acceptedSignals(acceptedSignals)
                    .build();
        return advance(definition, updated, now, SignalDisposition.ACCEPTED);
    }

    public WorkflowTransition onTimer(
            WorkflowDefinition<?, ?> definition,
            WorkflowExecutionState state,
            WorkflowTimer timer,
            Instant now
    ) {
        verifyDefinition(definition, state);
        Objects.requireNonNull(timer, "timer");
        Objects.requireNonNull(now, "now");
        if (isTerminal(state.status())) {
            return unchanged(state, SignalDisposition.DUPLICATE);
        }
        Map<String, NodeExecution> nodes = new LinkedHashMap<>(state.nodes());
        NodeExecution node = timer.phase() == WorkflowPhase.COMPENSATION
                ? state.compensationNodes().get(timer.nodeId())
                : nodes.get(timer.nodeId());
        if (node == null || !Objects.equals(node.logicalOperationId(), timer.logicalOperationId()) || node.generation() != timer.generation()) {
            return unchanged(state, SignalDisposition.IGNORED_STALE);
        }
        Instant scheduledTime = timer.purpose() == TimerPurpose.RETRY ? node.retryAt() : node.deadline();
        if (scheduledTime == null || now.isBefore(scheduledTime) || now.isBefore(timer.dueAt())) {
            return unchanged(state, SignalDisposition.IGNORED_STALE);
        }
        if (state.cancellationRequested() && timer.phase() == WorkflowPhase.FORWARD
                && (timer.purpose() == TimerPurpose.DELAY || timer.purpose() == TimerPurpose.DEADLINE)
                && (definition.nodes().get(timer.nodeId()) instanceof DelayNode
                || definition.nodes().get(timer.nodeId()) instanceof WaitForSignalNode)) {
            nodes.put(timer.nodeId(), node.skipped());
            WorkflowExecutionState updated = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.CANCEL_REQUESTED)
                    .nodes(nodes)
                    .cancellationRequested(true)
                    .build();
            return advance(definition, updated, now, SignalDisposition.ACCEPTED);
        }
        if (timer.purpose() == TimerPurpose.RETRY && node.status() == NodeExecutionStatus.RETRY_WAIT) {
            if (node.deadline() != null && !now.isBefore(node.deadline())) {
                Map<String, NodeExecution> timeoutNodes = new LinkedHashMap<>(state.nodes());
                Map<String, NodeExecution> timeoutCompensationNodes = new LinkedHashMap<>(state.compensationNodes());
                NodeExecution unknown = node.unknown("node deadline elapsed before retry dispatch");
                if (timer.phase() == WorkflowPhase.COMPENSATION) {
                    timeoutCompensationNodes.put(timer.nodeId(), unknown);
                } else {
                    timeoutNodes.put(timer.nodeId(), unknown);
                }
                return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .nodes(timeoutNodes)
                    .compensationNodes(timeoutCompensationNodes)
                    .failureReason("node deadline elapsed before retry dispatch")
                    .build(), List.of(), List.of(), SignalDisposition.ACCEPTED);
            }
            if (timer.phase() == WorkflowPhase.COMPENSATION) {
                Map<String, NodeExecution> compensationNodes = new LinkedHashMap<>(state.compensationNodes());
                compensationNodes.put(timer.nodeId(), node.retryPending());
                WorkflowExecutionState updated = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.COMPENSATING)
                    .nodes(nodes)
                    .compensationNodes(compensationNodes)
                    .build();
                return compensation.startOrFinish(definition, updated, now, SignalDisposition.ACCEPTED);
            }
            nodes.put(timer.nodeId(), node.retryPending());
            return advance(definition, ExecutionUpdate.from(state)
                    .status(ExecutionStatus.RUNNING)
                    .nodes(nodes)
                    .build(), now, SignalDisposition.ACCEPTED);
        }
        if (timer.purpose() == TimerPurpose.DELAY && timer.phase() == WorkflowPhase.FORWARD
                && definition.nodes().get(timer.nodeId()) instanceof DelayNode
                && node.status() == NodeExecutionStatus.DISPATCHED) {
            WorkflowSignal signal = signal("timer:" + timer.nodeId(), state, timer.nodeId(), node.logicalOperationId(), node.attemptId(), node.attempts(), timer.phase(), WorkflowSignalOutcome.SUCCEEDED, node.input(), WorkflowFailureClassification.UNCLASSIFIED, null, now);
            return accept(definition, state, signal, now);
        }
        if (timer.purpose() == TimerPurpose.DEADLINE && node.status() == NodeExecutionStatus.DISPATCHED
                && !(definition.nodes().get(timer.nodeId()) instanceof DelayNode)) {
            WorkflowSignal signal = signal("timeout:" + timer.nodeId(), state, timer.nodeId(), node.logicalOperationId(), node.attemptId(), node.attempts(), timer.phase(), WorkflowSignalOutcome.UNKNOWN, null, WorkflowFailureClassification.UNCLASSIFIED, "deadline elapsed; outcome is unknown", now);
            return accept(definition, state, signal, now);
        }
        return unchanged(state, SignalDisposition.IGNORED_STALE);
    }

    public WorkflowTransition cancel(WorkflowDefinition<?, ?> definition, WorkflowExecutionState state, Instant now) {
        verifyDefinition(definition, state);
        Objects.requireNonNull(now, "now");
        if (isTerminal(state.status())) {
            return unchanged(state, SignalDisposition.DUPLICATE);
        }
        WorkflowExecutionState updated = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.CANCEL_REQUESTED)
                    .cancellationRequested(true)
                    .build();
        return advance(definition, updated, now, SignalDisposition.ACCEPTED);
    }

    public WorkflowTransition retryIncident(
            WorkflowDefinition<?, ?> definition,
            WorkflowExecutionState state,
            Instant now
    ) {
        verifyDefinition(definition, state);
        Objects.requireNonNull(now, "now");
        if (state.status() != ExecutionStatus.NEEDS_ATTENTION) {
            throw new IllegalStateException("Only needs-attention executions can be retried");
        }
        if (state.nodes().values().stream().anyMatch(node -> node.status() == NodeExecutionStatus.UNKNOWN)
                || state.compensationNodes().values().stream().anyMatch(node -> node.status() == NodeExecutionStatus.UNKNOWN)) {
            throw new IllegalStateException("Unknown outcomes require participant reconciliation");
        }
        Map<String, NodeExecution> compensationNodes = new LinkedHashMap<>(state.compensationNodes());
        boolean failedCompensation = false;
        for (Map.Entry<String, NodeExecution> entry : compensationNodes.entrySet()) {
            if (entry.getValue().status() == NodeExecutionStatus.FAILED) {
                compensationNodes.put(entry.getKey(), entry.getValue().retryPending());
                failedCompensation = true;
            }
        }
        if (!failedCompensation) {
            throw new IllegalStateException("Incident is not eligible for automatic retry");
        }
        Set<String> succeededTasks = new LinkedHashSet<>();
        for (WorkflowNode workflowNode : definition.nodes().values()) {
            if (workflowNode instanceof TaskNode<?, ?> task
                    && state.nodes().get(task.id()).status() == NodeExecutionStatus.SUCCEEDED) {
                succeededTasks.add(task.id());
            }
        }
        CompensationPlan plan = compensationPlanner.plan(definition, succeededTasks);
        WorkflowExecutionState retrying = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.COMPENSATING)
                    .compensationNodes(compensationNodes)
                    .build();
        return compensation.dispatch(retrying, plan, now, SignalDisposition.ACCEPTED);
    }

    private WorkflowTransition advance(WorkflowDefinition<?, ?> definition, WorkflowExecutionState state, Instant now, SignalDisposition disposition) {
        verifyDefinition(definition, state);
        if (isTerminal(state.status())) {
            return unchanged(state, disposition);
        }
        Map<String, NodeExecution> nodes = new LinkedHashMap<>(state.nodes());
        Map<String, Object> outputs = new LinkedHashMap<>(state.outputs());
        Map<String, String> choices = new LinkedHashMap<>(state.choices());
        try {
            CompositeProgression.reconcile(definition.root(), state.input(), nodes, choices, outputs);
        } catch (ChoiceSelectionException exception) {
            WorkflowExecutionState incident = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .nodes(nodes)
                    .outputs(outputs)
                    .choices(choices)
                    .failureReason(exception.getMessage())
                    .build();
            return new WorkflowTransition(incident, List.of(), List.of(), disposition);
        }
        WorkflowExecutionState reconciled = ExecutionUpdate.from(state)
                    .nodes(nodes)
                    .outputs(outputs)
                    .choices(choices)
                    .build();

        if (reconciled.cancellationRequested() || reconciled.status() == ExecutionStatus.CANCEL_REQUESTED) {
            settleInternalWaits(definition, nodes);
            settleInternalComposites(definition.root(), nodes);
            WorkflowExecutionState settled = ExecutionUpdate.from(reconciled)
                    .status(ExecutionStatus.CANCEL_REQUESTED)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(true)
                    .build();
            if (hasUnknownForward(definition, settled)) {
                return new WorkflowTransition(ExecutionUpdate.from(settled)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(true)
                    .failureReason("cancelled execution has an unresolved external outcome")
                    .build(), List.of(), List.of(), disposition);
            }
            if (hasUnresolvedForward(definition, settled)) {
                return new WorkflowTransition(settled, List.of(), List.of(), disposition);
            }
            return compensation.startOrFinish(definition, settled, now, disposition);
        }
        NodeExecution root = nodes.get(definition.root().id());
        if (root.status() == NodeExecutionStatus.SUCCEEDED) {
            Object result = nodes.get(definition.outputNodeId()).output();
            outputs.put(definition.outputNodeId(), result);
            return new WorkflowTransition(ExecutionUpdate.from(reconciled)
                    .status(ExecutionStatus.COMPLETED)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(false)
                    .result(result)
                    .build(), List.of(), List.of(), disposition);
        }
        if (root.status() == NodeExecutionStatus.FAILED || root.status() == NodeExecutionStatus.UNKNOWN) {
            if (root.status() == NodeExecutionStatus.UNKNOWN) {
                return new WorkflowTransition(ExecutionUpdate.from(reconciled)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(false)
                    .failureReason(root.error())
                    .build(), List.of(), List.of(), disposition);
            }
            settleInternalWaits(definition, nodes);
            settleInternalComposites(definition.root(), nodes);
            WorkflowExecutionState failed = ExecutionUpdate.from(reconciled)
                    .status(ExecutionStatus.COMPENSATING)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(false)
                    .failureReason(root.error())
                    .build();
            if (hasUnknownForward(definition, failed)) {
                return new WorkflowTransition(ExecutionUpdate.from(failed)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(false)
                    .failureReason("failed execution has an unresolved external outcome")
                    .build(), List.of(), List.of(), disposition);
            }
            if (hasUnresolvedForward(definition, failed)) {
                return new WorkflowTransition(failed, List.of(), List.of(), disposition);
            }
            return compensation.startOrFinish(definition, failed, now, disposition);
        }
        List<WorkflowDispatch> dispatches = new ArrayList<>();
        List<WorkflowTimerIntent> timers = new ArrayList<>();
        int existingDispatches = (int) nodes.values().stream()
                .filter(node -> node.phase() == WorkflowPhase.FORWARD && node.status() == NodeExecutionStatus.DISPATCHED)
                .count();
        forwardDispatch.dispatchLeaves(definition.root(), nodes, reconciled.choices(), dispatches, timers, reconciled, now, existingDispatches);
        ExecutionStatus status = dispatches.isEmpty() && timers.isEmpty() ? ExecutionStatus.WAITING : ExecutionStatus.RUNNING;
        return new WorkflowTransition(ExecutionUpdate.from(reconciled)
                    .status(status)
                    .nodes(nodes)
                    .outputs(outputs)
                    .cancellationRequested(false)
                    .build(), dispatches, timers, disposition);
    }

    private static boolean hasUnresolvedForward(WorkflowDefinition<?, ?> definition, WorkflowExecutionState state) {
        return state.nodes().values().stream().anyMatch(node -> node.phase() == WorkflowPhase.FORWARD
                && definition.nodes().get(node.nodeId()) instanceof TaskNode<?, ?>
                && (node.status() == NodeExecutionStatus.DISPATCHED || node.status() == NodeExecutionStatus.UNKNOWN));
    }

    private static boolean hasUnknownForward(WorkflowDefinition<?, ?> definition, WorkflowExecutionState state) {
        return state.nodes().values().stream().anyMatch(node -> node.phase() == WorkflowPhase.FORWARD
                && definition.nodes().get(node.nodeId()) instanceof TaskNode<?, ?>
                && node.status() == NodeExecutionStatus.UNKNOWN);
    }

    private static void settleInternalWaits(WorkflowDefinition<?, ?> definition, Map<String, NodeExecution> nodes) {
        definition.nodes().values().stream()
                .filter(node -> node instanceof DelayNode || node instanceof WaitForSignalNode)
                .map(WorkflowNode::id)
                .map(nodes::get)
                .filter(Objects::nonNull)
                .filter(node -> node.status() == NodeExecutionStatus.PENDING
                        || node.status() == NodeExecutionStatus.ACTIVE
                        || node.status() == NodeExecutionStatus.DISPATCHED
                        || node.status() == NodeExecutionStatus.RETRY_WAIT)
                .forEach(node -> nodes.put(node.nodeId(), node.skipped()));
    }

    private static void settleInternalComposites(WorkflowNode node, Map<String, NodeExecution> nodes) {
        node.children().forEach(child -> settleInternalComposites(child, nodes));
        if (node instanceof TaskNode<?, ?> || node instanceof DelayNode || node instanceof WaitForSignalNode) {
            return;
        }
        NodeExecution execution = nodes.get(node.id());
        if (execution == null || (execution.status() != NodeExecutionStatus.PENDING && execution.status() != NodeExecutionStatus.ACTIVE)) {
            return;
        }
        boolean settled = node.children().stream()
                .map(child -> nodes.get(child.id()))
                .allMatch(child -> child != null && (child.status() == NodeExecutionStatus.SKIPPED
                        || child.status() == NodeExecutionStatus.SUCCEEDED
                        || child.status() == NodeExecutionStatus.COMPENSATED));
        if (settled) {
            nodes.put(node.id(), execution.skipped());
        }
    }

    private static RetryPolicy retryPolicy(WorkflowNode node) {
        if (node instanceof TaskNode<?, ?> task) {
            return task.retryPolicy();
        }
        return RetryPolicy.DEFAULT;
    }

    private static void validateOutput(WorkflowNode node, Object output) {
        if (output == null) {
            throw new IllegalArgumentException("Successful workflow outcomes require a non-null payload");
        }
        Class<?> expected = node instanceof WaitForSignalNode signal ? signal.payloadType() : node.outputType();
        if (!expected.isInstance(output)) {
            throw new IllegalArgumentException("Workflow outcome type does not match " + expected.getName());
        }
    }

    private static WorkflowSignal signal(String messageId, WorkflowExecutionState state, String nodeId, String logicalOperationId, String attemptId, int attempt, WorkflowPhase phase, WorkflowSignalOutcome outcome, Object payload, WorkflowFailureClassification classification, String error, Instant occurredAt) {
        return new WorkflowSignal(messageId, state.executionId(), nodeId, logicalOperationId, attemptId, attempt, phase, outcome, payload, classification, null, error, state.payloadVersion(), occurredAt);
    }

    private static boolean isTerminal(ExecutionStatus status) {
        return status == ExecutionStatus.COMPLETED
                || status == ExecutionStatus.CANCELLED
                || status == ExecutionStatus.COMPENSATED
                || status == ExecutionStatus.NEEDS_ATTENTION
                || status == ExecutionStatus.FAILED;
    }

    private static WorkflowNode requireNode(WorkflowDefinition<?, ?> definition, String nodeId) {
        WorkflowNode node = definition.nodes().get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Unknown workflow node: " + nodeId);
        }
        return node;
    }

    private static void verifyDefinition(WorkflowDefinition<?, ?> definition, WorkflowExecutionState state) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(state, "state");
        if (!definition.name().equals(state.workflowName()) || definition.version() != state.definitionVersion()) {
            throw new WorkflowCorrelationException("Definition name/version does not match execution state");
        }
        if (!definition.nodes().keySet().containsAll(state.nodes().keySet())) {
            throw new WorkflowCorrelationException("Execution state contains nodes outside the supplied definition");
        }
        boolean foreignCompensation = state.compensationNodes().keySet().stream()
                .map(id -> id.startsWith("comp:") ? id.substring("comp:".length()) : id)
                .anyMatch(id -> !definition.nodes().containsKey(id));
        if (foreignCompensation) {
            throw new WorkflowCorrelationException("Execution state contains compensation nodes outside the supplied definition");
        }
    }

    private static WorkflowTransition withTimer(WorkflowTransition transition, WorkflowTimerIntent timer) {
        List<WorkflowTimerIntent> timers = new ArrayList<>(transition.timers());
        timers.add(timer);
        return new WorkflowTransition(transition.state(), transition.dispatches(), timers, transition.signalDisposition());
    }

    private static WorkflowTransition unchanged(WorkflowExecutionState state, SignalDisposition disposition) {
        return new WorkflowTransition(state, List.of(), List.of(), disposition);
    }

}
