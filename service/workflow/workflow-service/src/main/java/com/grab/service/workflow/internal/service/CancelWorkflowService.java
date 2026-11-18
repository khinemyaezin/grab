package com.grab.service.workflow.internal.service;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowRuntime;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort;
import com.grab.service.workflow.internal.port.inbound.CancelWorkflowUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class CancelWorkflowService implements CancelWorkflowUseCase {

    private final WorkflowRuntime runtime;
    private final Clock clock;

    public CancelWorkflowService(WorkflowRuntime runtime, @Qualifier("workflowClock") Clock clock) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public WorkflowRuntimeCommandPort.OperationResult execute(WorkflowRuntimeCommandPort.CancelMessage message) {
        Instant now = clock.instant();
        WorkflowRuntime.RuntimeOperationResult result = runtime.cancel(
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
