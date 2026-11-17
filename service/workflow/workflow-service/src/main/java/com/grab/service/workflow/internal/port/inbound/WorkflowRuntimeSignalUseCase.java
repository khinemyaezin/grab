package com.grab.service.workflow.internal.port.inbound;

import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalResult;

public interface WorkflowRuntimeSignalUseCase {

    SignalResult accept(SignalMessage message);
}
