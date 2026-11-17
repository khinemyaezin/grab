package com.grab.service.workflow.internal.port.inbound;

import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.CancelMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;

public interface CancelWorkflowUseCase {

    OperationResult execute(CancelMessage message);
}
