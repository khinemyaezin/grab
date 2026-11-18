package com.grab.framework.workflow.internal;

import com.grab.framework.workflow.ExecutionStatus;
import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.WorkflowExecutionState;

import java.util.Map;

public final class ExecutionUpdate {

    private final WorkflowExecutionState original;
    private ExecutionStatus status;
    private Map<String, NodeExecution> nodes;
    private Map<String, NodeExecution> compensationNodes;
    private Map<String, Object> outputs;
    private Map<String, String> choices;
    private Map<String, String> acceptedSignals;
    private boolean cancellationRequested;
    private String failureReason;
    private Object result;

    private ExecutionUpdate(WorkflowExecutionState original) {
        this.original = original;
        status = original.status();
        nodes = original.nodes();
        compensationNodes = original.compensationNodes();
        outputs = original.outputs();
        choices = original.choices();
        acceptedSignals = original.acceptedSignals();
        cancellationRequested = original.cancellationRequested();
        failureReason = original.failureReason();
        result = original.result();
    }

    public static ExecutionUpdate from(WorkflowExecutionState state) {
        return new ExecutionUpdate(state);
    }

    public ExecutionUpdate status(ExecutionStatus status) {
        this.status = status;
        return this;
    }

    public ExecutionUpdate nodes(Map<String, NodeExecution> nodes) {
        this.nodes = nodes;
        return this;
    }

    public ExecutionUpdate compensationNodes(Map<String, NodeExecution> compensationNodes) {
        this.compensationNodes = compensationNodes;
        return this;
    }

    public ExecutionUpdate outputs(Map<String, Object> outputs) {
        this.outputs = outputs;
        return this;
    }

    public ExecutionUpdate choices(Map<String, String> choices) {
        this.choices = choices;
        return this;
    }

    public ExecutionUpdate acceptedSignals(Map<String, String> acceptedSignals) {
        this.acceptedSignals = acceptedSignals;
        return this;
    }

    public ExecutionUpdate cancellationRequested(boolean cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
        return this;
    }

    public ExecutionUpdate failureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    public ExecutionUpdate result(Object result) {
        this.result = result;
        return this;
    }

    public WorkflowExecutionState build() {
        return new WorkflowExecutionState(original.executionId(), original.workflowName(), original.definitionVersion(),
                original.callerScope(), original.payloadVersion(), status, original.input(), nodes, compensationNodes,
                outputs, choices, acceptedSignals, cancellationRequested, failureReason, result);
    }
}
