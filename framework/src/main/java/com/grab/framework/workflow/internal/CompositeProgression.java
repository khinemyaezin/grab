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
import com.grab.framework.workflow.WaitForSignalNode;
import com.grab.framework.workflow.WorkflowNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CompositeProgression {

    private CompositeProgression() {
    }

    public static void reconcile(WorkflowNode node, Object input, Map<String, NodeExecution> nodes, Map<String, String> choices, Map<String, Object> outputs) {
        NodeExecution execution = nodes.get(node.id());
        if (isTerminalNode(execution)) {
            return;
        }
        if (execution.status() == NodeExecutionStatus.PENDING) {
            execution = execution.active(input);
            nodes.put(node.id(), execution);
        }
        switch (node) {
            case TaskNode<?, ?> task -> { }
            case WaitForSignalNode wait -> { }
            case DelayNode delay -> { }
            case PassThroughNode<?> passThrough -> {
                Object result = passThrough.type().cast(input);
                nodes.put(node.id(), execution.succeed(result, null));
                outputs.put(node.id(), result);
                return;
            }
            case LocalMapNode<?, ?> map -> {
                Object result = map.apply(input);
                complete(node, execution, result, nodes, outputs);
            }
            case BoundTaskNode<?, ?, ?, ?> bound -> {
                WorkflowNode task = bound.task();
                NodeExecution child = nodes.get(task.id());
                if (child.status() == NodeExecutionStatus.PENDING) {
                    Object request = child.input() == null ? bound.request(execution.input()) : child.input();
                    reconcile(task, request, nodes, choices, outputs);
                } else if (child.status() == NodeExecutionStatus.SUCCEEDED) {
                    Object result = bound.combine(execution.input(), child.output());
                    complete(node, execution, result, nodes, outputs);
                } else if (child.status() == NodeExecutionStatus.FAILED || child.status() == NodeExecutionStatus.UNKNOWN) {
                    nodes.put(node.id(), execution.fail(child.error()));
                }
            }
            case JoinedParallelNode<?, ?, ?, ?> join -> {
                List<WorkflowNode> branches = join.children();
                for (WorkflowNode branch : branches) {
                    reconcile(branch, input, nodes, choices, outputs);
                    NodeExecution child = nodes.get(branch.id());
                    if (child.status() == NodeExecutionStatus.FAILED || child.status() == NodeExecutionStatus.UNKNOWN) {
                        nodes.put(node.id(), execution.fail(child.error()));
                        return;
                    }
                }
                WorkflowNode left = branches.getFirst();
                WorkflowNode right = branches.getLast();
                NodeExecution leftExecution = nodes.get(left.id());
                NodeExecution rightExecution = nodes.get(right.id());
                if (leftExecution.status() == NodeExecutionStatus.SUCCEEDED
                        && rightExecution.status() == NodeExecutionStatus.SUCCEEDED) {
                    Object result = join.join(leftExecution.output(), rightExecution.output());
                    complete(node, execution, result, nodes, outputs);
                }
            }
            case SequenceNode sequence -> {
                Object childInput = input;
                for (WorkflowNode child : sequence.children()) {
                    NodeExecution childExecution = nodes.get(child.id());
                    if (childExecution.status() == NodeExecutionStatus.SKIPPED || childExecution.status() == NodeExecutionStatus.SUCCEEDED) {
                        childInput = childExecution.output();
                        continue;
                    }
                    reconcile(child, childInput, nodes, choices, outputs);
                    childExecution = nodes.get(child.id());
                    if (childExecution.status() == NodeExecutionStatus.SUCCEEDED || childExecution.status() == NodeExecutionStatus.SKIPPED) {
                        childInput = childExecution.output();
                        continue;
                    }
                    if (childExecution.status() == NodeExecutionStatus.FAILED || childExecution.status() == NodeExecutionStatus.UNKNOWN) {
                        nodes.put(node.id(), execution.fail(childExecution.error()));
                    }
                    return;
                }
                Object result = childInput;
                nodes.put(node.id(), execution.succeed(result, null));
                outputs.put(node.id(), result);
                return;
            }
            case ParallelNode parallel -> {
                boolean allComplete = true;
                List<Object> results = new ArrayList<>();
                for (WorkflowNode branch : parallel.branches()) {
                    reconcile(branch, input, nodes, choices, outputs);
                    NodeExecution branchExecution = nodes.get(branch.id());
                    if (branchExecution.status() == NodeExecutionStatus.FAILED || branchExecution.status() == NodeExecutionStatus.UNKNOWN) {
                        nodes.put(node.id(), execution.fail(branchExecution.error()));
                        return;
                    }
                    if (branchExecution.status() != NodeExecutionStatus.SUCCEEDED && branchExecution.status() != NodeExecutionStatus.SKIPPED) {
                        allComplete = false;
                    }
                    results.add(branchExecution.output());
                }
                if (allComplete) {
                    List<Object> immutableResult = List.copyOf(results);
                    nodes.put(node.id(), execution.succeed(immutableResult, null));
                    outputs.put(node.id(), immutableResult);
                }
                return;
            }
            case ChoiceNode<?> choice -> {
                String selected = choices.get(node.id());
                if (selected == null) {
                    selected = select(choice, input);
                    if (selected == null) {
                        selected = choice.defaultBranch();
                    }
                    if (selected == null) {
                        throw new ChoiceSelectionException("Choice requires a selected or default branch", null);
                    }
                    if (!choice.branches().containsKey(selected)) {
                        throw new ChoiceSelectionException("Choice selector returned unknown branch: " + selected, null);
                    }
                    choices.put(node.id(), selected);
                    String chosenBranch = selected;
                    choice.branches().forEach((label, child) -> {
                        if (!label.equals(chosenBranch)) {
                            markSkipped(child, nodes);
                        }
                    });
                }
                WorkflowNode branch = choice.branches().get(selected);
                reconcile(branch, input, nodes, choices, outputs);
                NodeExecution branchExecution = nodes.get(branch.id());
                if (branchExecution.status() == NodeExecutionStatus.SUCCEEDED || branchExecution.status() == NodeExecutionStatus.SKIPPED) {
                    nodes.put(node.id(), execution.succeed(branchExecution.output(), null));
                    outputs.put(node.id(), branchExecution.output());
                } else if (branchExecution.status() == NodeExecutionStatus.FAILED || branchExecution.status() == NodeExecutionStatus.UNKNOWN) {
                    nodes.put(node.id(), execution.fail(branchExecution.error()));
                }
            }
        }
    }

    private static String select(ChoiceNode<?> choice, Object input) {
        if (!choice.legacySelectorFailureHandling()) {
            return choice.selectResolvedInput(input);
        }
        try {
            return choice.selectResolvedInput(input);
        } catch (RuntimeException exception) {
            throw new ChoiceSelectionException("Choice selector failed: " + exception.getMessage(), exception);
        }
    }

    private static void complete(WorkflowNode node, NodeExecution execution, Object result,
                                 Map<String, NodeExecution> nodes, Map<String, Object> outputs) {
        NodeExecution succeeded = execution.succeed(result, null);
        nodes.put(node.id(), succeeded);
        outputs.put(node.id(), result);
    }

    static boolean isTerminalNode(NodeExecution execution) {
        return execution != null && (execution.status() == NodeExecutionStatus.SUCCEEDED
                || execution.status() == NodeExecutionStatus.SKIPPED
                || execution.status() == NodeExecutionStatus.FAILED
                || execution.status() == NodeExecutionStatus.UNKNOWN
                || execution.status() == NodeExecutionStatus.COMPENSATED);
    }

    private static void markSkipped(WorkflowNode node, Map<String, NodeExecution> nodes) {
        nodes.put(node.id(), nodes.get(node.id()).skipped());
        node.children().forEach(child -> markSkipped(child, nodes));
    }

    public static final class ChoiceSelectionException extends RuntimeException {
        private ChoiceSelectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
