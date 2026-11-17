package com.grab.service.workflow.internal.port.inbound;

import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartResult;

public interface StartWorkflowUseCase {

    StartResult execute(StartMessage message);
}
