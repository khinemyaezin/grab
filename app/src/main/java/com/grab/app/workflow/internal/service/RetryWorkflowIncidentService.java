package com.grab.app.workflow.internal.service;

import com.grab.app.workflow.internal.port.inbound.RetryWorkflowIncidentUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowRuntime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class RetryWorkflowIncidentService implements RetryWorkflowIncidentUseCase {

    private final WorkflowRuntime runtime;
    private final Clock clock;

    public RetryWorkflowIncidentService(WorkflowRuntime runtime, @Qualifier("workflowClock") Clock clock) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public WorkflowRuntimeCommandPort.OperationResult execute(WorkflowRuntimeCommandPort.RetryIncidentMessage message) {
        Instant now = clock.instant();
        WorkflowRuntime.RuntimeOperationResult result = runtime.retryIncident(
                message.callerScope(),
                message.executionId().getValue(),
                message.reason(),
                now
        );
        return new WorkflowRuntimeCommandPort.OperationResult(
                new CommonId(result.executionId()),
                result.status().name()
        );
    }
}
