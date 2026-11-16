package com.grab.framework.workflow.internal;

import com.grab.framework.workflow.CompensationAction;
import com.grab.framework.workflow.CompensationInput;
import com.grab.framework.workflow.CompensationPlan;
import com.grab.framework.workflow.CompensationPlanner;
import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.NodeExecutionStatus;
import com.grab.framework.workflow.SignalDisposition;
import com.grab.framework.workflow.TaskNode;
import com.grab.framework.workflow.TimerPurpose;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowDispatch;
import com.grab.framework.workflow.WorkflowExecutionState;
import com.grab.framework.workflow.WorkflowNode;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowTimerIntent;
import com.grab.framework.workflow.WorkflowTransition;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompensationPolicy {

    private final int parallelLimit;
    private final CompensationPlanner compensationPlanner;

    public CompensationPolicy(int parallelLimit, CompensationPlanner compensationPlanner) {
        this.parallelLimit = parallelLimit;
        this.compensationPlanner = compensationPlanner;
    }

    public WorkflowTransition startOrFinish(WorkflowDefinition<?, ?> definition, WorkflowExecutionState state, Instant now, SignalDisposition disposition) {
        Set<String> succeededTasks = new LinkedHashSet<>();
        boolean irreversible = false;
        for (WorkflowNode workflowNode : definition.nodes().values()) {
            if (workflowNode instanceof TaskNode<?, ?> task) {
                NodeExecution execution = state.nodes().get(task.id());
                if (execution != null && execution.status() == NodeExecutionStatus.SUCCEEDED) {
                    succeededTasks.add(task.id());
                    if (task.compensation().isEmpty()) {
                        irreversible = true;
                    }
                }
            }
        }
        if (irreversible) {
            return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .failureReason("successful task has no compensation")
                    .build(), List.of(), List.of(), disposition);
        }
        CompensationPlan plan = compensationPlanner.plan(definition, succeededTasks);
        if (plan.isEmpty()) {
            ExecutionStatus terminal = state.cancellationRequested() ? ExecutionStatus.CANCELLED : ExecutionStatus.COMPENSATED;
            return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(terminal)
                    .build(), List.of(), List.of(), disposition);
        }
        Map<String, NodeExecution> compensationNodes = new LinkedHashMap<>(state.compensationNodes());
        for (CompensationAction action : plan.waves().stream().flatMap(List::stream).toList()) {
            String compensationNodeId = compensationNodeId(action.nodeId());
            if (!compensationNodes.containsKey(compensationNodeId)) {
                NodeExecution source = state.nodes().get(action.nodeId());
                CompensationInput<?, ?> compensationInput = new CompensationInput<>(source.input(), source.output());
                NodeExecution obligation = NodeExecution.pendingCompensation(compensationNodeId, compensationInput);
                compensationNodes.put(compensationNodeId, obligation);
            }
        }
        WorkflowExecutionState compensating = ExecutionUpdate.from(state)
                    .status(ExecutionStatus.COMPENSATING)
                    .compensationNodes(compensationNodes)
                    .build();
        return dispatch( compensating, plan, now, disposition);
    }

    public WorkflowTransition dispatch(WorkflowExecutionState state, CompensationPlan plan, Instant now, SignalDisposition disposition) {
        Map<String, NodeExecution> compensationNodes = new LinkedHashMap<>(state.compensationNodes());
        List<WorkflowDispatch> dispatches = new ArrayList<>();
        List<WorkflowTimerIntent> timers = new ArrayList<>();
        int existingDispatches = (int) compensationNodes.values().stream()
                .filter(node -> node.status() == NodeExecutionStatus.DISPATCHED)
                .count();
        if (compensationNodes.values().stream().anyMatch(node -> node.status() == NodeExecutionStatus.FAILED || node.status() == NodeExecutionStatus.UNKNOWN)) {
            return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(ExecutionStatus.NEEDS_ATTENTION)
                    .compensationNodes(compensationNodes)
                    .failureReason("compensation failed")
                    .build(), List.of(), List.of(), disposition);
        }
        for (List<CompensationAction> wave : plan.waves()) {
            boolean wavePending = wave.stream().anyMatch(action -> compensationNodes.get(compensationNodeId(action.nodeId())).status() != NodeExecutionStatus.COMPENSATED);
            if (!wavePending) {
                continue;
            }
            for (CompensationAction action : wave) {
                if (dispatches.size() + existingDispatches >= parallelLimit) {
                    break;
                }
                String id = compensationNodeId(action.nodeId());
                NodeExecution execution = compensationNodes.get(id);
                if (execution.status() == NodeExecutionStatus.ACTIVE || execution.status() == NodeExecutionStatus.PENDING) {
                    String logical = state.executionId() + ":compensation:" + action.nodeId();
                    String attemptId = SignalCorrelation.attemptId(logical, execution.attempts() + 1);
                    Instant deadline = execution.deadline() == null ? now.plus(action.timeout()) : execution.deadline();
                    NodeExecution dispatched = execution.dispatch(logical, attemptId, execution.input(), deadline);
                    compensationNodes.put(id, dispatched);
                    dispatches.add(new WorkflowDispatch(
                            state.callerScope(),
                            state.workflowName(),
                            state.definitionVersion(),
                            state.payloadVersion(),
                            id,
                            action.operation(),
                            logical,
                            attemptId,
                            dispatched.attempts(),
                            WorkflowPhase.COMPENSATION,
                            execution.input()
                    ));
                    timers.add(new WorkflowTimerIntent(id, logical, TimerPurpose.DEADLINE, dispatched.generation(), deadline, WorkflowPhase.COMPENSATION));
                }
            }
            break;
        }
        if (compensationNodes.values().stream().allMatch(node -> node.status() == NodeExecutionStatus.COMPENSATED)) {
            ExecutionStatus terminal = state.cancellationRequested() ? ExecutionStatus.CANCELLED : ExecutionStatus.COMPENSATED;
            return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(terminal)
                    .compensationNodes(compensationNodes)
                    .build(), List.of(), List.of(), disposition);
        }
        return new WorkflowTransition(ExecutionUpdate.from(state)
                    .status(ExecutionStatus.COMPENSATING)
                    .compensationNodes(compensationNodes)
                    .build(), dispatches, timers, disposition);
    }

    private static String compensationNodeId(String forwardNodeId) {
        return "comp:" + forwardNodeId;
    }

}
