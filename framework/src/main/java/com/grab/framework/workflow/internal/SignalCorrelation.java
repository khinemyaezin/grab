package com.grab.framework.workflow.internal;

import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.WorkflowCorrelationException;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowSignal;

import java.util.Map;
import java.util.Objects;

public final class SignalCorrelation {

    private SignalCorrelation() {
    }

    public static NodeExecution resolve(WorkflowSignal signal, Map<String, NodeExecution> forward,
                                        Map<String, NodeExecution> compensation) {
        NodeExecution node = signal.phase() == WorkflowPhase.COMPENSATION
                ? compensation.get(signal.nodeId())
                : forward.get(signal.nodeId());
        if (node == null) {
            throw new WorkflowCorrelationException("Unknown signal node: " + signal.nodeId());
        }
        if (!Objects.equals(node.logicalOperationId(), signal.logicalOperationId())) {
            throw new WorkflowCorrelationException("Signal logical operation does not match the active node");
        }
        if (node.phase() != signal.phase()) {
            throw new WorkflowCorrelationException("Signal phase does not match the active node");
        }
        String expectedAttemptId = attemptId(node.logicalOperationId(), signal.attempt());
        if (signal.attempt() > node.attempts() || !expectedAttemptId.equals(signal.attemptId())) {
            throw new WorkflowCorrelationException("Signal does not match an issued attempt");
        }
        return node;
    }

    public static String attemptId(String operationId, int attempt) {
        return operationId + ":attempt-" + attempt;
    }
}
