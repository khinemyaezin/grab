package com.grab.service.workflow.internal.port.inbound;

import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.RetryIncidentMessage;

public interface RetryWorkflowIncidentUseCase {

    OperationResult execute(RetryIncidentMessage message);
}
