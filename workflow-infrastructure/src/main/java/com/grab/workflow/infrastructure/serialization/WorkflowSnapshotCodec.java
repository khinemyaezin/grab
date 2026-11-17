package com.grab.workflow.infrastructure.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.grab.framework.workflow.BoundTaskNode;
import com.grab.framework.workflow.ChoiceNode;
import com.grab.framework.workflow.JoinedParallelNode;
import com.grab.framework.workflow.LocalMapNode;
import com.grab.framework.workflow.CompensationInput;
import com.grab.framework.workflow.DelayNode;
import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.NodeAttempt;
import com.grab.framework.workflow.NodeExecutionStatus;
import com.grab.framework.workflow.ParallelNode;
import com.grab.framework.workflow.PassThroughNode;
import com.grab.framework.workflow.SequenceNode;
import com.grab.framework.workflow.TaskNode;
import com.grab.framework.workflow.WaitForSignalNode;
import com.grab.framework.workflow.WorkflowCorrelationException;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowExecutionState;
import com.grab.framework.workflow.WorkflowNode;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowUnit;
import com.grab.workflow.infrastructure.entity.WorkflowExecutionEntity;
import com.grab.workflow.infrastructure.entity.WorkflowNodeExecutionEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorkflowSnapshotCodec {

    private static final JsonMapper JSON = new JsonMapper();
    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() { };
    private final WorkflowPayloadCodec codecs;

    public WorkflowSnapshotCodec(WorkflowPayloadCodec codecs) {
        this.codecs = Objects.requireNonNull(codecs, "codecs");
    }

    public WorkflowExecutionState restore(WorkflowExecutionEntity execution, List<WorkflowNodeExecutionEntity> rows,
                                           WorkflowDefinition<?, ?> definition) {
        if (!definition.name().equals(execution.getDefinitionName())
                || definition.version() != execution.getDefinitionVersion()) {
            throw new WorkflowCorrelationException("Snapshot does not match the pinned definition");
        }
        WorkflowPayloadCodec codec = codecs.require(definition, execution.getPayloadVersion());
        Object input = codec.readTyped(execution.getInputJson(), definition.inputType());
        Map<String, String> choices = readMap(execution.getChoicesJson());
        Map<String, String> acceptedSignals = readMap(execution.getAcceptedSignalsJson());
        Map<String, WorkflowNodeExecutionEntity> forward = new LinkedHashMap<>();
        Map<String, WorkflowNodeExecutionEntity> compensation = new LinkedHashMap<>();
        for (WorkflowNodeExecutionEntity row : rows) {
            Map<String, WorkflowNodeExecutionEntity> phase = row.getPhase() == WorkflowPhase.FORWARD
                    ? forward : compensation;
            phase.put(row.getNodeId(), row);
        }
        if (!forward.keySet().equals(definition.nodes().keySet())) {
            throw new IllegalStateException("Snapshot nodes do not match the pinned definition");
        }
        Map<String, NodeExecution> nodes = new LinkedHashMap<>();
        restoreNode(definition.root(), input, forward, choices, nodes, codec);
        Map<String, NodeExecution> compensated = new LinkedHashMap<>();
        for (WorkflowNodeExecutionEntity row : compensation.values()) {
            String id = row.getNodeId();
            if (!id.startsWith("comp:")) {
                throw new IllegalStateException("Invalid compensation snapshot node: " + id);
            }
            String sourceId = id.substring("comp:".length());
            NodeExecution source = nodes.get(sourceId);
            WorkflowNode sourceDefinition = definition.nodes().get(sourceId);
            if (!(sourceDefinition instanceof TaskNode<?, ?> task) || task.compensation().isEmpty()
                    || source == null || source.status() != NodeExecutionStatus.SUCCEEDED) {
                throw new IllegalStateException("Invalid compensation source: " + sourceId);
            }
            CompensationInput<?, ?> resolvedInput = new CompensationInput<>(source.input(), source.output());
            verifyValue(row.getInputJson(), resolvedInput, codec);
            WorkflowUnit output = codec.readTyped(row.getOutputJson(), WorkflowUnit.class);
            compensated.put(id, toNode(row, resolvedInput, output));
        }
        Map<String, Object> outputs = new LinkedHashMap<>();
        for (NodeExecution node : nodes.values()) {
            if (node.output() != null) {
                outputs.put(node.nodeId(), node.output());
            }
        }
        Object result = execution.getStatus() == ExecutionStatus.COMPLETED ? outputs.get(definition.outputNodeId()) : null;
        verifyValue(execution.getResultJson(), result, codec);
        return new WorkflowExecutionState(execution.getId(), execution.getDefinitionName(),
                execution.getDefinitionVersion(), execution.getCallerScope(), execution.getPayloadVersion(),
                execution.getStatus(), input, nodes, compensated, outputs, choices, acceptedSignals,
                execution.isCancellationRequested(), execution.getFailureReason(), result);
    }

    private Object restoreNode(WorkflowNode node, Object suppliedInput, Map<String, WorkflowNodeExecutionEntity> rows,
                               Map<String, String> choices, Map<String, NodeExecution> restored,
                               WorkflowPayloadCodec codec) {
        WorkflowNodeExecutionEntity row = rows.get(node.id());
        Object input = row.getInputJson() == null ? null : suppliedInput;
        verifyValue(row.getInputJson(), input, codec);
        Object childOutput = switch (node) {
            case SequenceNode sequence -> restoreSequence(sequence, suppliedInput, rows, choices, restored, codec);
            case ChoiceNode<?> choice -> restoreChoice(choice, suppliedInput, rows, choices, restored, codec);
            case ParallelNode parallel -> restoreParallel(parallel, suppliedInput, rows, choices, restored, codec);
            case LocalMapNode<?, ?> map -> codec.readTyped(row.getOutputJson(), map.outputType());
            case JoinedParallelNode<?, ?, ?, ?> join -> {
                for (WorkflowNode branch : join.children()) {
                    restoreNode(branch, suppliedInput, rows, choices, restored, codec);
                }
                validateCompletedChildren(join, row, restored);
                yield codec.readTyped(row.getOutputJson(), join.outputType());
            }
            case BoundTaskNode<?, ?, ?, ?> bound -> {
                TaskNode<?, ?> task = bound.task();
                WorkflowNodeExecutionEntity child = rows.get(task.id());
                Object request = codec.readTyped(child.getInputJson(), task.inputType());
                if (request != null && input == null) {
                    throw new IllegalStateException("Bound task request has no workflow input: " + task.id());
                }
                restoreNode(task, request, rows, choices, restored, codec);
                validateCompletedChildren(bound, row, restored);
                yield codec.readTyped(row.getOutputJson(), bound.outputType());
            }
            case TaskNode<?, ?> task -> codec.readTyped(row.getOutputJson(), task.outputType());
            case DelayNode delay -> input;
            case PassThroughNode<?> passThrough -> passThrough.type().cast(input);
            case WaitForSignalNode wait -> codec.readTyped(row.getOutputJson(), wait.outputType());
            default -> throw new IllegalStateException("Unexpected value: " + node);
        };
        Object output = row.getStatus() == NodeExecutionStatus.SUCCEEDED ? childOutput : null;
        verifyValue(row.getOutputJson(), output, codec);
        restored.put(node.id(), toNode(row, input, output));
        return output;
    }

    private static void validateCompletedChildren(WorkflowNode node, WorkflowNodeExecutionEntity row,
                                                  Map<String, NodeExecution> restored) {
        if (row.getStatus() != NodeExecutionStatus.SUCCEEDED) {
            return;
        }
        for (WorkflowNode child : node.children()) {
            NodeExecution execution = restored.get(child.id());
            if (execution.status() != NodeExecutionStatus.SUCCEEDED) {
                throw new IllegalStateException("Completed workflow node has an incomplete child: " + child.id());
            }
        }
    }

    private Object restoreSequence(SequenceNode sequence, Object input, Map<String, WorkflowNodeExecutionEntity> rows,
                                   Map<String, String> choices, Map<String, NodeExecution> restored,
                                   WorkflowPayloadCodec codec) {
        Object value = input;
        for (WorkflowNode child : sequence.children()) {
            value = restoreNode(child, value, rows, choices, restored, codec);
        }
        return value;
    }

    private Object restoreChoice(ChoiceNode<?> choice, Object input, Map<String, WorkflowNodeExecutionEntity> rows,
                                 Map<String, String> choices, Map<String, NodeExecution> restored,
                                 WorkflowPayloadCodec codec) {
        String selection = choices.get(choice.id());
        if (selection != null && !choice.branches().containsKey(selection)) {
            throw new IllegalStateException("Unknown persisted branch: " + selection);
        }
        Object value = null;
        for (Map.Entry<String, WorkflowNode> branch : choice.branches().entrySet()) {
            boolean selected = branch.getKey().equals(selection);
            Object output = restoreNode(branch.getValue(), selected ? input : null, rows, choices, restored, codec);
            if (selected) {
                value = output;
            }
        }
        return value;
    }

    private Object restoreParallel(ParallelNode parallel, Object input, Map<String, WorkflowNodeExecutionEntity> rows,
                                   Map<String, String> choices, Map<String, NodeExecution> restored,
                                   WorkflowPayloadCodec codec) {
        List<Object> values = new ArrayList<>();
        for (WorkflowNode branch : parallel.branches()) {
            Object output = restoreNode(branch, input, rows, choices, restored, codec);
            values.add(output);
        }
        return values.contains(null) ? null : List.copyOf(values);
    }

    private NodeExecution toNode(WorkflowNodeExecutionEntity row, Object input, Object output) {
        int generation = Math.toIntExact(row.getNodeGeneration());
        NodeAttempt attempt = new NodeAttempt(row.getLogicalOperationId(), row.getAttemptId(), row.getAttempts(),
                row.getDeadlineAt(), row.getRetryAt(), generation);
        return new NodeExecution(row.getNodeId(), row.getStatus(), row.getPhase(), attempt, input, output,
                row.getErrorMessage(), row.getAcceptedOutcomeJson());
    }

    private void verifyValue(String json, Object value, WorkflowPayloadCodec codec) {
        String restored = value == null ? null : codec.writeTyped(value);
        JsonNode expected = codec.readTyped(json, JsonNode.class);
        JsonNode actual = codec.readTyped(restored, JsonNode.class);
        if (!Objects.equals(expected, actual)) {
            throw new IllegalStateException("Stored workflow value does not match its definition and upstream values");
        }
    }

    private Map<String, String> readMap(String value) {
        if (value == null) {
            return Map.of();
        }
        try {
            return JSON.readValue(value, STRING_MAP);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Invalid workflow snapshot map", exception);
        }
    }
}
