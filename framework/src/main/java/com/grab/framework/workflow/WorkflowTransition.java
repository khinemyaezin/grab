package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;

public record WorkflowTransition(
        WorkflowExecutionState state,
        List<WorkflowDispatch> dispatches,
        List<WorkflowTimerIntent> timers,
        SignalDisposition signalDisposition
) {

    public WorkflowTransition {
        state = Objects.requireNonNull(state, "state");
        dispatches = List.copyOf(Objects.requireNonNull(dispatches, "dispatches"));
        timers = List.copyOf(Objects.requireNonNull(timers, "timers"));
        signalDisposition = Objects.requireNonNull(signalDisposition, "signalDisposition");
    }

    public WorkflowTransition(WorkflowExecutionState state, List<WorkflowDispatch> dispatches) {
        this(state, dispatches, List.of(), SignalDisposition.ACCEPTED);
    }
}
