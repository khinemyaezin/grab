package com.grab.app.workflow.internal.port.inbound;

import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.CancelMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.OperationResult;

public interface CancelWorkflowUseCase {

    OperationResult execute(CancelMessage message);
}
