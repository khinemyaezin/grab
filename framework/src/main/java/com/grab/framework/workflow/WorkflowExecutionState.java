package com.grab.framework.workflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record WorkflowExecutionState(
        String executionId,
        String workflowName,
        int definitionVersion,
        String callerScope,
        int payloadVersion,
        ExecutionStatus status,
        Object input,
        Map<String, NodeExecution> nodes,
        Map<String, NodeExecution> compensationNodes,
        Map<String, Object> outputs,
        Map<String, String> choices,
        Map<String, String> acceptedSignals,
        boolean cancellationRequested,
        String failureReason,
        Object result
) {

    public WorkflowExecutionState {
        executionId = requireText(executionId, "executionId");
        workflowName = requireText(workflowName, "workflowName");
        if (definitionVersion < 1) {
            throw new IllegalArgumentException("definitionVersion must be positive");
        }
        callerScope = requireText(callerScope, "callerScope");
        if (payloadVersion < 1) {
            throw new IllegalArgumentException("payloadVersion must be positive");
        }
        status = Objects.requireNonNull(status, "status");
        input = Objects.requireNonNull(input, "input");
        nodes = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(nodes, "nodes")));
        compensationNodes = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(compensationNodes, "compensationNodes")));
        outputs = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(outputs, "outputs")));
        choices = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(choices, "choices")));
        acceptedSignals = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(acceptedSignals, "acceptedSignals")));
    }

    public NodeExecution node(String nodeId) {
        NodeExecution execution = nodes.get(nodeId);
        if (execution == null) {
            throw new IllegalArgumentException("Unknown workflow node: " + nodeId);
        }
        return execution;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
