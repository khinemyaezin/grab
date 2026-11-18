package com.grab.app.workflow.internal.port.inbound;

import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.StartMessage;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort.StartResult;

public interface StartWorkflowUseCase {

    StartResult execute(StartMessage message);
}
