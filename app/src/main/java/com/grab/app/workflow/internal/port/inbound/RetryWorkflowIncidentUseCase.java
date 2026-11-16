package com.grab.app.workflow.internal.port.inbound;

import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.RetryIncidentMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.OperationResult;

public interface RetryWorkflowIncidentUseCase {

    OperationResult execute(RetryIncidentMessage message);
}
