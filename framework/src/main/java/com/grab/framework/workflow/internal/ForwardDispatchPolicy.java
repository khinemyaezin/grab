package com.grab.framework.workflow.internal;

import com.grab.framework.workflow.BoundTaskNode;
import com.grab.framework.workflow.ChoiceNode;
import com.grab.framework.workflow.JoinedParallelNode;
import com.grab.framework.workflow.LocalMapNode;
import com.grab.framework.workflow.DelayNode;
import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.NodeExecutionStatus;
import com.grab.framework.workflow.ParallelNode;
import com.grab.framework.workflow.PassThroughNode;
import com.grab.framework.workflow.SequenceNode;
import com.grab.framework.workflow.TaskNode;
import com.grab.framework.workflow.TimerPurpose;
import com.grab.framework.workflow.WaitForSignalNode;
import com.grab.framework.workflow.WorkflowDispatch;
import com.grab.framework.workflow.WorkflowExecutionState;
import com.grab.framework.workflow.WorkflowNode;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowTimerIntent;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ForwardDispatchPolicy {

    private final int parallelLimit;

    public ForwardDispatchPolicy(int parallelLimit) {
        this.parallelLimit = parallelLimit;
    }

    public void dispatchLeaves(WorkflowNode node, Map<String, NodeExecution> nodes, Map<String, String> choices, List<WorkflowDispatch> dispatches, List<WorkflowTimerIntent> timers, WorkflowExecutionState state, Instant now, int existingDispatches) {
        if (dispatches.size() + existingDispatches >= parallelLimit) {
            return;
        }
        NodeExecution execution = nodes.get(node.id());
        switch (node) {
            case TaskNode<?, ?> task -> {
                if (execution.status() != NodeExecutionStatus.ACTIVE && execution.status() != NodeExecutionStatus.PENDING) {
                    return;
                }
                validateInput(task.inputType(), execution.input());
                String logical = state.executionId() + ":forward:" + node.id();
                String attemptId = SignalCorrelation.attemptId(logical, execution.attempts() + 1);
                Instant deadline = execution.deadline() == null ? now.plus(task.timeout()) : execution.deadline();
                NodeExecution dispatched = execution.dispatch(logical, attemptId, execution.input(), deadline);
                nodes.put(node.id(), dispatched);
                dispatches.add(new WorkflowDispatch(
                        state.callerScope(),
                        state.workflowName(),
                        state.definitionVersion(),
                        state.payloadVersion(),
                        node.id(),
                        task.operation(),
                        logical,
                        attemptId,
                        dispatched.attempts(),
                        WorkflowPhase.FORWARD,
                        execution.input()
                ));
                timers.add(new WorkflowTimerIntent(node.id(), logical, TimerPurpose.DEADLINE, dispatched.generation(), deadline, WorkflowPhase.FORWARD));
                return;
            }
            case WaitForSignalNode signal -> {
                if (execution.status() != NodeExecutionStatus.ACTIVE && execution.status() != NodeExecutionStatus.PENDING) {
                    return;
                }
                String logical = state.executionId() + ":forward:" + node.id();
                String attemptId = SignalCorrelation.attemptId(logical, execution.attempts() + 1);
                Instant deadline = now.plus(signal.timeout());
                NodeExecution dispatched = execution.dispatch(logical, attemptId, execution.input(), deadline);
                nodes.put(node.id(), dispatched);
                timers.add(new WorkflowTimerIntent(node.id(), logical, TimerPurpose.DEADLINE, dispatched.generation(), deadline, WorkflowPhase.FORWARD));
                return;
            }
            case DelayNode delay -> {
                if (execution.status() != NodeExecutionStatus.ACTIVE && execution.status() != NodeExecutionStatus.PENDING) {
                    return;
                }
                String logical = state.executionId() + ":forward:" + node.id();
                String attemptId = SignalCorrelation.attemptId(logical, execution.attempts() + 1);
                Instant due = now.plus(delay.delay());
                NodeExecution dispatched = execution.dispatch(logical, attemptId, execution.input(), due);
                nodes.put(node.id(), dispatched);
                timers.add(new WorkflowTimerIntent(node.id(), logical, TimerPurpose.DELAY, dispatched.generation(), due, WorkflowPhase.FORWARD));
                return;
            }
            case LocalMapNode<?, ?> map -> { }
            case BoundTaskNode<?, ?, ?, ?> bound ->
                    dispatchLeaves(bound.task(), nodes, choices, dispatches, timers, state, now, existingDispatches);
            case JoinedParallelNode<?, ?, ?, ?> join -> {
                for (WorkflowNode branch : join.children()) {
                    if (!CompositeProgression.isTerminalNode(nodes.get(branch.id()))) {
                        dispatchLeaves(branch, nodes, choices, dispatches, timers, state, now, existingDispatches);
                    }
                }
            }
            case PassThroughNode<?> passThrough -> {
                return;
            }
            case SequenceNode sequence -> {
                for (WorkflowNode child : sequence.children()) {
                    if (CompositeProgression.isTerminalNode(nodes.get(child.id()))) {
                        continue;
                    }
                    dispatchLeaves(child, nodes, choices, dispatches, timers, state, now, existingDispatches);
                    return;
                }
                return;
            }
            case ParallelNode parallel -> {
                for (WorkflowNode branch : parallel.branches()) {
                    if (dispatches.size() + existingDispatches >= parallelLimit) {
                        return;
                    }
                    if (!CompositeProgression.isTerminalNode(nodes.get(branch.id()))) {
                        dispatchLeaves(branch, nodes, choices, dispatches, timers, state, now, existingDispatches);
                    }
                }
                return;
            }
            case ChoiceNode<?> choice -> {
                String selected = choices.get(choice.id());
                if (selected != null) {
                    dispatchLeaves(choice.branches().get(selected), nodes, choices, dispatches, timers, state, now, existingDispatches);
                }
                return;
            }
        }
    }

    private static void validateInput(Class<?> expected, Object input) {
        if (input == null || !expected.isInstance(input)) {
            throw new IllegalArgumentException("Resolved input does not match node input type " + expected.getName());
        }
    }

}
